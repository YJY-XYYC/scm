package com.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String path;
    
    private String component;
    
    private String icon;
    
    private Long parentId;
    
    private Integer sort;
    
    private Boolean hidden;
    
    // 非数据库字段，用于存储子菜单
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Menu> children;
}