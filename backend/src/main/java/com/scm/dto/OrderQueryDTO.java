package com.scm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderNo;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
} 