package com.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.ibatis.type.JdbcType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Types;

@Data
@TableName("sys_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String code;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private Integer forbidden; // 0-启用，1-禁用
    @TableField("property")
    private Integer property; // 物品属性(1-原料,2-半成品,3-成品)
    private String image;
    @TableField(value = "description", jdbcType = JdbcType.LONGVARCHAR)
    private String description;
    @TableField("library_coding")
    @JsonProperty("library_coding")
    private String libraryCoding; // 库位编码


    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}