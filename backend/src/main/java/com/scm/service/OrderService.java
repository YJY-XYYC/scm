package com.scm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Order;
import com.scm.dto.CreateOrderDTO;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    IPage<Order> getOrderList(Page<Order> page, String orderNo, Integer status, String startTime, String endTime, Long userId);
    Order getOrderDetail(Long id);
    void payOrder(Long id);
    void shipOrder(Long id);
    void cancelOrder(Long id);
    void completeOrder(Long id);
    Long getIncrease(LocalDateTime since);
    BigDecimal getTotalSales();
    BigDecimal getLastWeekSales();
    List<Map<String, Object>> getWeeklyTrend(LocalDateTime date);
    List<Map<String, Object>> getMonthlyTrend(LocalDateTime date);
    void createOrder(CreateOrderDTO createOrderDTO);
    List<Map<String, Object>> getWeeklySalesTrend(LocalDateTime start, LocalDateTime end);
    List<Map<String, Object>> getMonthlySalesTrend(LocalDateTime start, LocalDateTime end);
    List<Map<String, Object>> selectSalesRank(LocalDateTime start, LocalDateTime end);
    List<Map<String, Object>> getWeeklyOrderTrend(LocalDateTime start, LocalDateTime end);
    List<Map<String, Object>> getMonthlyOrderTrend(LocalDateTime start, LocalDateTime end);
    boolean checkOrderNoExists(String orderNo);
}