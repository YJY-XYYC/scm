package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.User;
import com.scm.mapper.UserMapper;
import com.scm.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User getByUsername(String username) {
        Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
        log.info("Querying user by username: {}", username);
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
        log.info("Query result: {}", user);
        return user;
    }
    
    @Override
    public IPage<User> getUserList(Page<User> page, String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        return this.page(page, wrapper);
    }
    
    @Override
    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
    }
    
    @Override
    public Long getIncrease(LocalDateTime since) {
        return baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, since));
    }
    
    @Override
    public List<Map<String, Object>> getRoleDistribution() {
        return baseMapper.selectRoleDistribution();
    }
} 