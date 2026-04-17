package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scm.entity.SupplierItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface SupplierItemMapper extends BaseMapper<SupplierItem> {
    // 根据供应商ID查询供应商产品关联信息，包含产品名称
    @Select("SELECT si.* FROM sys_supplier_item si WHERE si.supplier_id = #{supplierId}")
    List<Map<String, Object>> selectItemsBySupplierId(@Param("supplierId") Long supplierId);

    // 根据产品名称查询供应商ID
    @Select("SELECT supplier_id FROM sys_supplier_item WHERE product_name = #{productName}")
    List<Long> selectSupplierIdsByProductName(@Param("productName") String productName);

    // 查询所有不同的产品名称
    @Select("SELECT DISTINCT product_name FROM sys_supplier_item WHERE product_name IS NOT NULL AND product_name != ''")
    List<String> selectDistinctProductNames();

    // 根据产品名称查询属性
    @Select("SELECT property FROM sys_supplier_item WHERE product_name = #{productName} LIMIT 1")
    Integer selectPropertyByProductName(@Param("productName") String productName);
}