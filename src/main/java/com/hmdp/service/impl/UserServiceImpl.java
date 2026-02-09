package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.hmdp.convert.UserConvert;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since
 */

@Slf4j // 日志注解
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource // 或 @Autowired
    // 不能直接调用Mapper接口的，，必须要有实例化对象
    private UserMapper userMapper; // 实例变量，非静态

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 发送验证码的效果在这里实现

        // 正则表达式校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 手机号格式不正确
            return Result.fail("手机号格式不正确！");
        }
        /*
         * // 手机号格式正确
         * // 使用随机数工具包生成6位验证码
         * String code = RandomUtil.randomNumbers(6);
         * 
         * // 保存code到session
         * session.setAttribute("code", code);
         */

        // 改成用redis存储
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 发送短信验证码
        // 这里模拟一下就可以了
        log.debug("发送短信验证码成功，验证码:{}", code); // 日志参数用 {} 占位符，而非字符串拼接（+）
        // TODO 发送短信验证码并保存验证码
        return Result.ok();
    }

    @Override
    public Result logIn(LoginFormDTO loginForm, HttpSession session) {

        String phone = loginForm.getPhone();
        // 输入的验证码
        String codein = loginForm.getCode();
        // User user = userMapper.selectUserByPhone(phone);
        // // session中的验证码
        // String codesession = (String) session.getAttribute("code");

        // 取redis中的
        String codesession = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);

        if (codein == null || RegexUtils.isCodeInvalid(codein)) {
            log.debug("验证码无效，验证码不是:{}", codein);
            return Result.fail("验证码不正确");
        }
        /*
         * // else if (user != null && codesession.equals(codein)) {
         * // log.debug("登录成功");
         * // // 获取用户信息，保存到Session中
         * // // session.setAttribute("user", user);
         * // // 传进去用dto传保证数据安全
         * // session.setAttribute("user", UserConvert.convertStudent(user));
         * // return Result.ok();
         * // } else if (user == null && codesession.equals(codein)) {
         * // log.debug("正在注册，请按要求输入信息");
         * 
         * // user = createnewUser(phone);
         * // userMapper.insert(user);
         * // // session.setAttribute("user", user);
         * // // 传进去用dto传保证数据安全
         * // session.setAttribute("user", UserConvert.convertStudent(user));
         * 
         * // // 把下面创建新用户设置成函数
         * // // Scanner sc = new Scanner(System.in);
         * // // user.setPhone(phone);
         * // // log.debug("请输入你的密码");
         * // // String newuserpassword = sc.nextLine();
         * // // user.setPassword(newuserpassword);
         * // // log.debug("请输入你的用户名");
         * // // String newusernickname = sc.nextLine();
         * // // user.setNickName(newusernickname);
         * // // log.debug("请上传头像，默认为空");
         * // // String newusericon = sc.nextLine();
         * // // user.setIcon(newusericon);
         * // // // 插入到数据库中
         * // // userMapper.insert(user);
         * // // // 保存到session
         * // // session.setAttribute("user", user);
         * // // // 4. 关闭 Scanner（必须！避免资源占用）
         * // // sc.close();
         * 
         * // return Result.ok();
         * // }
         */
        // 上面很多无用判断， 简化代码如下

        if (!codein.equals(codesession)) {
            return Result.fail("验证码错误");
        }
        User user = userMapper.selectUserByPhone(phone);
        // 对象为空才创建
        if (user == null) {
            log.debug("正在注册");

            user = createnewUser(phone);
            userMapper.insert(user);

        }
        // session.setAttribute("user", user);
        // 传进去用dto传保证数据安全

        // session.setAttribute("user", UserConvert.convertStudent(user));

        // 保存到redis中
        UserDTO userDto = UserConvert.convertStudent(user);
        // 生成随机的token作为登陆令牌返回到客户端
        String token = UUID.randomUUID().toString();
        // 把userdto转成hashmap的形式
        Map<String, String> userDtoMap = UserConvert.converttoMap(userDto);
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userDtoMap);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 更新token有效期在拦截器中实现，因为是连续一段时间不操作才会清除token

        return Result.ok(token);

    }

    // 创建新用户
    public User createnewUser(String ph) {

        User user = new User();
        user.setPhone(ph);
        // 这里就用随机数演示了——默认不输入名字的情况
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        return user;
    }

}
