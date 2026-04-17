package com.scm.controller;

import com.scm.common.Result;
import com.scm.entity.Product;
import com.scm.service.UserService;
import com.scm.service.OrderService;
import com.scm.service.ProductService;
import com.scm.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(defaultValue = "week") String type,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        try {
            Map<String, Object> data = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastWeek = now.minus(7, ChronoUnit.DAYS);

            // 获取基础统计数据
            data.put("userCount", userService.count());
            data.put("productCount", productService.count(new LambdaQueryWrapper<Product>().eq(Product::getProperty, 3).eq(Product::getForbidden, 1)));
            data.put("orderCount", orderService.count());
            data.put("totalSales", orderService.getTotalSales());
            
            // 获取增长数据
            data.put("userIncrease", userService.getIncrease(lastWeek));
            data.put("productIncrease", productService.getIncrease(lastWeek));
            data.put("orderIncrease", orderService.getIncrease(lastWeek));
            data.put("salesIncrease", calculateIncrease(
                orderService.getTotalSales(),
                orderService.getLastWeekSales()
            ));

            // 获取热销商品（根据参数决定时间范围）
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            
            // 如果提供了自定义时间范围
            if (start != null && end != null) {
                try {
                    startTime = LocalDateTime.parse(start);
                } catch (Exception e) {
                    // 假设只有日期部分，格式为 yyyy-MM-dd
                    LocalDate startDate = LocalDate.parse(start);
                    startTime = startDate.atStartOfDay();
                }
                
                try {
                    endTime = LocalDateTime.parse(end);
                } catch (Exception e) {
                    // 假设只有日期部分，格式为 yyyy-MM-dd
                    LocalDate endDate = LocalDate.parse(end);
                    endTime = endDate.atTime(23, 59, 59);
                }
            } else {
                // 根据类型设置默认时间范围
                if ("week".equals(type)) {
                    // 获取本周数据
                    startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
                    endTime = startTime.plusDays(7);
                } else if ("month".equals(type)) {
                    // 获取本月数据
                    startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    endTime = startTime.plusMonths(1);
                } else if ("year".equals(type)) {
                    // 获取本年数据
                    startTime = now.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    endTime = startTime.plusYears(1);
                }
                // 如果type不是以上值，使用null表示不限制时间范围
            }
            
            data.put("topProduct", orderService.selectSalesRank(startTime, endTime));

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> getOrderTrend(@RequestParam String type) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> data;
            
            if ("week".equals(type)) {
                // 获取本周数据
                LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfWeek = startOfWeek.plusDays(7);
                data = orderService.getWeeklyOrderTrend(startOfWeek, endOfWeek);
            } else {
                // 获取本年数据
                LocalDateTime startOfYear = now.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfYear = startOfYear.plusYears(1);
                data = orderService.getMonthlyOrderTrend(startOfYear, endOfYear);
            }
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取订单趋势失败", e);
            return Result.error("获取订单趋势失败：" + e.getMessage());
        }
    }

    @GetMapping("/user-distribution")
    public Result<List<Map<String, Object>>> getUserDistribution() {
        try {
            List<Map<String, Object>> data = userService.getRoleDistribution();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户分布数据失败", e);
            return Result.error("获取用户分布数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> getCategoryDistribution() {
        try {
            List<Map<String, Object>> data = productService.getCategoryDistribution();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商品分类分布数据失败", e);
            return Result.error("获取商品分类分布数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/recent-activities")
    public Result<List<Map<String, Object>>> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = dashboardService.getRecentActivities();
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取最近活动失败", e);
            return Result.error("获取最近活动失败：" + e.getMessage());
        }
    }

    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> getSalesTrend(@RequestParam String type, 
                                                          @RequestParam(required = false) String start, 
                                                          @RequestParam(required = false) String end) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> data;
            
            if ("week".equals(type)) {
                LocalDateTime startTime;
                LocalDateTime endTime;
                
                // 如果提供了自定义时间范围
                if (start != null && end != null) {
                    // 尝试解析日期时间，如果失败则解析为日期并转换为开始时间
                    try {
                        startTime = LocalDateTime.parse(start);
                    } catch (Exception e) {
                        // 假设只有日期部分，格式为 yyyy-MM-dd
                        LocalDate startDate = LocalDate.parse(start);
                        startTime = startDate.atStartOfDay();
                    }
                    
                    // 尝试解析日期时间，如果失败则解析为日期并转换为结束时间
                    try {
                        endTime = LocalDateTime.parse(end);
                    } catch (Exception e) {
                        // 假设只有日期部分，格式为 yyyy-MM-dd
                        LocalDate endDate = LocalDate.parse(end);
                        endTime = endDate.atTime(23, 59, 59);
                    }
                } else {
                    // 默认获取本周数据（向后兼容）
                    startTime = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
                    endTime = startTime.plusDays(7);
                }
                
                data = orderService.getWeeklySalesTrend(startTime, endTime);
            } else {
                LocalDateTime startTime;
                LocalDateTime endTime;
                
                // 如果提供了自定义时间范围
                if (start != null && end != null) {
                    // 尝试解析日期时间，如果失败则解析为日期并转换为开始时间
                    try {
                        startTime = LocalDateTime.parse(start);
                    } catch (Exception e) {
                        // 假设只有日期部分，格式为 yyyy-MM-dd
                        LocalDate startDate = LocalDate.parse(start);
                        startTime = startDate.atStartOfDay();
                    }
                    
                    // 尝试解析日期时间，如果失败则解析为日期并转换为结束时间
                    try {
                        endTime = LocalDateTime.parse(end);
                    } catch (Exception e) {
                        // 假设只有日期部分，格式为 yyyy-MM-dd
                        LocalDate endDate = LocalDate.parse(end);
                        endTime = endDate.atTime(23, 59, 59);
                    }
                } else {
                    // 获取当年全年12月份的数据
                    startTime = now.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    endTime = now.withMonth(12).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59);
                }
                
                data = orderService.getMonthlySalesTrend(startTime, endTime);
            }
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取销售趋势失败", e);
            return Result.error("获取销售趋势失败：" + e.getMessage());
        }
    }

    private double calculateIncrease(BigDecimal current, BigDecimal last) {
        if (last == null || last.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return current.subtract(last)
                .divide(last, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }
}