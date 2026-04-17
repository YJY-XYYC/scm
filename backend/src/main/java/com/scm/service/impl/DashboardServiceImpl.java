package com.scm.service.impl;

import com.scm.dto.DashboardDTO;
import com.scm.entity.Order;
import com.scm.entity.Product;
import com.scm.entity.User;
import com.scm.service.DashboardService;
import com.scm.service.ProductService;
import com.scm.service.OrderService;
import com.scm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();

        // 获取统计数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minus(1, ChronoUnit.DAYS);

        // 设置增长数据
        Map<String, Long> increaseData = new HashMap<>();
        increaseData.put("userIncrease", userService.getIncrease(yesterday));
        increaseData.put("productIncrease", productService.getIncrease(yesterday));
        increaseData.put("orderIncrease", orderService.getIncrease(yesterday));

        dto.setIncreaseData(increaseData);

        // 设置其他数据
        dto.setTopProduct(productService.getTopProduct(10));
        dto.setCategoryDistribution(productService.getCategoryDistribution());

        return dto;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 获取最近24小时内的活动
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);
        
        // 获取最近订单
        List<Order> recentOrders = orderService.lambdaQuery()
                .ge(Order::getCreateTime, yesterday)
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 5")
                .list();
                
        for (Order order : recentOrders) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "order");
            activity.put("content", "新增订单 #" + order.getOrderNo());
            activity.put("time", order.getCreateTime());
            activities.add(activity);
        }
        
        // 获取最近商品变更
        List<Product> recentProduct = productService.lambdaQuery()
                .ge(Product::getCreateTime, yesterday)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT 5")
                .list();
                
        for (Product product : recentProduct) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "product");
            activity.put("content", "新商品上架: " + product.getName());
            activity.put("time", product.getCreateTime());
            activities.add(activity);
        }
        
        // 获取最近用户活动
        List<User> recentUsers = userService.lambdaQuery()
                .ge(User::getCreateTime, yesterday)
                .orderByDesc(User::getCreateTime)
                .last("LIMIT 5")
                .list();
                
        for (User user : recentUsers) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "user");
            activity.put("content", "用户" + user.getUsername() + "完成注册");
            activity.put("time", user.getCreateTime());
            activities.add(activity);
        }
        
        // 按时间排序
        activities.sort((a, b) -> ((LocalDateTime)b.get("time")).compareTo((LocalDateTime)a.get("time")));
        
        // 只返回最近10条
        return activities.stream().limit(10).collect(Collectors.toList());
    }
}