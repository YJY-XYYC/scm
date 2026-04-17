package com.scm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    
    User getByUsername(String username);
    
    IPage<User> getUserList(Page<User> page, String username);
    
    void addUser(User user);
    
    Long getIncrease(LocalDateTime since);
    
    List<Map<String, Object>> getRoleDistribution();
} 