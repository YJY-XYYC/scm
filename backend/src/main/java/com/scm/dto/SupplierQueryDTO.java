package com.scm.dto;

import lombok.Data;

@Data
public class SupplierQueryDTO {
    private String name;
    private String code;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
} 