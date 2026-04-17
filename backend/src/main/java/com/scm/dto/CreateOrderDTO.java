package com.scm.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {
    
    private Long userId;
    private String orderNo;
    private String consignee;
    private String phone;
    private String address;
    private List<OrderItemDTO> items;
    
    @Data
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}