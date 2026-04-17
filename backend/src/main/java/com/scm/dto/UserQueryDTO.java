package com.scm.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String name;
    private String role;
    private Integer status;
} 