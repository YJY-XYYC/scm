package com.scm.controller;

import com.scm.common.Result;
import com.scm.dto.LoginDTO;
import com.scm.dto.RegisterDTO;
import com.scm.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        try {
            log.info("Login request received: {}", loginDTO);
            
            // 获取sessionId（使用sessionId作为验证码存储的key）
            String sessionId = request.getSession().getId();
            
            // 使用新的登录方法，包含验证码验证
            String loginResult = authService.login(
                loginDTO.getUsername(), 
                loginDTO.getPassword(), 
                loginDTO.getCaptcha(), 
                sessionId
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("result", loginResult);
            return Result.success(data);
        } catch (Exception e) {
            log.error("Login failed", e);
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody RegisterDTO registerDTO) {
        try {
            log.info("Register request received: {}", registerDTO.getUsername());
            boolean result = authService.register(registerDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Register failed", e);
            return Result.error(e.getMessage());
        }
    }
}