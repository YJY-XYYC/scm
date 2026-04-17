package com.scm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物料实体类
 * 对应sys_material表
 */
@Data
@TableName("sys_material")
public class Material implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料类型 (1:原料, 2:半成品, 3:成品)
     */
    private Integer materialType;

    /**
     * 所需材料 (格式：物料ID*数量/物料ID*数量，例如：1*2/2*1)
     */
    private String requiredMaterials;

    /**
     * 所需时间(小时)
     */
    private Double requiredTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态 (0:禁用, 1:启用)
     */
    private Integer status;

    /**
     * 单位
     */
    private String unit;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    // 手动添加必要的setter方法
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}