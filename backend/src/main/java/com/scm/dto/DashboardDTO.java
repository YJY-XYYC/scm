package com.scm.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardDTO {
    private Long userCount;
    private Long productCount;
    private Long orderCount;
    private Long supplierCount;
    private List<Map<String, Object>> topProduct;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> recentOrders;
    private Map<String, Long> increaseData;
} 