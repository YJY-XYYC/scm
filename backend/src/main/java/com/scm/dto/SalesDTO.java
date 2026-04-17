package com.scm.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesDTO {
    /**
     * 日期
     */
    private String date;
    
    /**
     * 订单数量
     */
    private Integer orderCount;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 平均金额
     */
    private BigDecimal averageAmount;
} 