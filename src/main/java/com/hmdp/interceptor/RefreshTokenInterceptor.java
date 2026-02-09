package com.hmdp.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import com.hmdp.convert.UserConvert;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;

import io.netty.util.internal.StringUtil;

// 只用作更新tokrn使用

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        /*
         * // 获取session
         * HttpSession session = request.getSession();
         * 
         * // 获取session中的用户
         * Object user = session.getAttribute("user");
         */
        // 使用redis实现

        String token = request.getHeader("authorization");
        if (StringUtil.isNullOrEmpty(token)) {
            // 不存在不拦截
            return true;
        }

        // TODO 这里先用 (Map) 强制转换，以后再改
        Map<String, String> userDtoMap = (Map) stringRedisTemplate.opsForHash()
                .entries(RedisConstants.LOGIN_USER_KEY + token);

        // 判断用户是否存在
        if (userDtoMap.isEmpty()) {
            // 不存在不拦截

            return true;
        }

        // 转成UserDto
        UserDTO userDTO = UserConvert.converttoUserDTO(userDtoMap);

        // 存在
        // 保存到Threadlocal
        UserHolder.saveUser(userDTO);

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
        UserHolder.removeUser();
    }

}
