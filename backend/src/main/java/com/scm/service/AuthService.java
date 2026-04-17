package com.scm.service;

import com.scm.dto.RegisterDTO;

public interface AuthService {
    String login(String username, String password);
    String login(String username, String password, String captcha, String sessionId);
    boolean register(RegisterDTO registerDTO);
    void saveCaptcha(String sessionId, String captcha);
}