package com.scm.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesStatisticsDTO {
    private StatisticsData current;
    private StatisticsData compare;
    
    @Data
    public static class StatisticsData {
        private BigDecimal totalSales;
        private Integer orderCount;
        private BigDecimal averageAmount;
    }
} 