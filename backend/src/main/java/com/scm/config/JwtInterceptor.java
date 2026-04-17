package com.scm.config;

import com.scm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import io.jsonwebtoken.Claims;
import org.springframework.util.StringUtils;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 解析token
                Claims claims = jwtUtil.parseToken(token);
                // 验证token是否过期
                if (claims.getExpiration().before(new Date())) {
                    throw new RuntimeException("Token has expired");
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Invalid token");
            }
        }
        throw new RuntimeException("Token is required");
    }
}