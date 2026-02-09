package com.hmdp.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.hmdp.convert.UserConvert;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;

import ch.qos.logback.core.pattern.Converter;
import io.netty.util.internal.StringUtil;

public class LoginInterceptor implements HandlerInterceptor {
    /*
     * // 这里也不需要度redis了只需要读线程中的对象就可以了
     * // private StringRedisTemplate stringRedisTemplate;
     * 
     * // public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
     * // this.stringRedisTemplate = stringRedisTemplate;
     * // }
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        /*
         * // 获取session
         * HttpSession session = request.getSession();
         * 
         * // 获取session中的用户
         * Object user = session.getAttribute("user");
         */
        // 下面这些都在Refreshtoken这个拦截器实现
        // 这里只用判断用户存不存在线程中就可以了
        /*
         * String token = request.getHeader("authorization");
         * if (StringUtil.isNullOrEmpty(token)) {
         * // 不存在拦截
         * response.setStatus(401);
         * return false;
         * }
         * 
         * // TODO 这里先用 (Map) 强制转换，以后再改
         * Map<String, String> userDtoMap = (Map) stringRedisTemplate.opsForHash()
         * .entries(RedisConstants.LOGIN_USER_KEY + token);
         * 
         * // 判断用户是否存在
         * if (userDtoMap.isEmpty()) {
         * // 不存在拦截
         * response.setStatus(401);
         * return false;
         * }
         * 
         * // 转成UserDto
         * UserDTO userDTO = UserConvert.converttoUserDTO(userDtoMap);
         * 
         * // 存在
         * // 保存到Threadlocal
         * UserHolder.saveUser(userDTO);
         */

        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            response.setStatus(401);
            return false;
        }

        return true;
    }

    // 不需要这个来删除线程对象，在哪里创建就在哪删掉

    // public void afterCompletion(HttpServletRequest request, HttpServletResponse
    // response, Object handler,
    // @Nullable Exception ex) throws Exception {
    // UserHolder.removeUser();
    // }

}
