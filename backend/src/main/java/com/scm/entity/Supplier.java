package com.scm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName("sys_supplier")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String remark;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 供应商产品关联信息(非数据库字段)
     */
    @TableField(exist = false)
    private List<Map<String, Object>> supplierProducts;

    public List<Map<String, Object>> getSupplierProducts() {
        return supplierProducts;
    }

    public void setSupplierProducts(List<Map<String, Object>> supplierProducts) {
        this.supplierProducts = supplierProducts;
    }
}