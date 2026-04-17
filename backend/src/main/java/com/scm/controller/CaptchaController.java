package com.scm.controller;

import com.scm.service.AuthService;
import com.scm.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/generate")
    public void generateCaptcha(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("Captcha generation request received");
            
            // 生成验证码
            String code = CaptchaUtil.generateCode();
            log.info("Generated captcha code: {}", code);
            
            // 获取sessionId作为验证码存储的key
            String sessionId = request.getSession().getId();
            log.info("Session ID: {}", sessionId);
            
            // 使用AuthService保存验证码
            authService.saveCaptcha(sessionId, code);
            log.info("Captcha saved for session: {}", sessionId);
            
            // 设置响应头
            response.setContentType("image/png");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域
            
            // 生成验证码图片并输出
            BufferedImage image = CaptchaUtil.generateImage(code);
            log.info("Captcha image generated successfully");
            
            // 确保输出流正确关闭
            try (ServletOutputStream out = response.getOutputStream()) {
                ImageIO.write(image, "png", out);
                out.flush();
                log.info("Captcha image written to output stream");
            }
        } catch (Exception e) {
            log.error("Error generating captcha: {}", e.getMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate captcha");
            } catch (IOException ex) {
                log.error("Error sending error response: {}", ex.getMessage());
            }
        }
    }
}