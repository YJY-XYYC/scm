package com.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_order")
public class Sales {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private BigDecimal totalAmount;
    
    private Integer status;
    
    private LocalDateTime createTime;
} 