package com.scm.service.impl;

import com.scm.dto.RegisterDTO;
import com.scm.entity.User;
import com.scm.service.AuthService;
import com.scm.service.UserService;
import com.scm.utils.CaptchaUtil;
import com.scm.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserService userService;
    
    // 简单的内存存储验证码，实际项目中可以使用Redis
    private static final ConcurrentHashMap<String, String> CAPTCHA_CACHE = new ConcurrentHashMap<>();
    
    @Override
    public String login(String username, String password, String captcha, String sessionId) {
        // 验证验证码
        if (!validateCaptcha(sessionId, captcha)) {
            log.error("Invalid captcha for session: {}", sessionId);
            throw new RuntimeException("验证码错误");
        }
        
        log.info("Attempting login with username: {}", username);
        
        User user = userService.getByUsername(username);
        if (user == null) {
            log.error("User not found: {}", username);
            throw new RuntimeException("用户不存在");
        }
        
        log.info("Found user: {}", user.getUsername());
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            log.error("User account is disabled: {}", username);
            throw new RuntimeException("用户账号已禁用");
        }
        
        // 简单密码比对（后续应使用PasswordEncoder进行安全的密码校验）
        if (!user.getPassword().equals(password)) {
            log.error("Password mismatch for user: {}", username);
            throw new RuntimeException("密码错误");
        }
        
        // 生成JWT token，包含用户ID、用户名和角色信息
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("Login successful for user: {}, token generated", username);
        return token;
    }
    
    @Override
    public String login(String username, String password) {
        // 兼容旧版本的登录方法
        log.error("Login attempted without captcha: {}", username);
        throw new RuntimeException("请提供验证码");
    }
    
    @Override
    public void saveCaptcha(String sessionId, String captcha) {
        CAPTCHA_CACHE.put(sessionId, captcha);
        log.info("Captcha saved for session: {}", sessionId);
        // 设置验证码5分钟过期
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
                CAPTCHA_CACHE.remove(sessionId);
                log.info("Captcha expired and removed for session: {}", sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private boolean validateCaptcha(String sessionId, String inputCaptcha) {
        if (sessionId == null || inputCaptcha == null) {
            return false;
        }
        String storedCaptcha = CAPTCHA_CACHE.get(sessionId);
        return CaptchaUtil.validateCaptcha(inputCaptcha, storedCaptcha);
    }
    
    // 辅助方法，用于简单登录逻辑，可根据需要调整或删除
    public Map<String, Object> simpleLogin(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }
    
    @Override
    public boolean register(RegisterDTO registerDTO) {
        log.info("尝试注册用户: {}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        User existingUser = userService.getByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            log.error("用户名已存在: {}", registerDTO.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        // 使用传入的头像，如果为空则设置为空字符串
        // 头像显示逻辑将由前端处理，使用用户名或真实姓名的首字母作为默认显示
        String avatarValue = registerDTO.getAvatar() != null && !registerDTO.getAvatar().isEmpty() 
            ? registerDTO.getAvatar() 
            : "";
        
        // 对于注册时传入的头像，这里直接存储，不再做长度限制
        // 因为实际使用时会通过专门的头像上传接口上传到文件系统
        
        user.setAvatar(avatarValue);
        // 使用前端传递的角色信息，如果为空则默认使用"user"
        user.setRole(registerDTO.getRole() != null && !registerDTO.getRole().isEmpty() ? registerDTO.getRole() : "user");
        // 设置用户默认状态为0（禁用），需要管理员同意后启用
        user.setStatus(0);
        user.setDeleted(0);
        
        // 保存用户到数据库
        boolean saved = userService.save(user);
        log.info("用户注册成功: {}, 保存结果: {}, 角色: {}, 状态: {}", 
                registerDTO.getUsername(), saved, user.getRole(), user.getStatus());
        return saved;
    }
}