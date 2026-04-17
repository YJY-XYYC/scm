package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scm.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    @Select("SELECT oi.*, p.name as product_name, p.image as product_image " +
            "FROM sys_order_item oi " +
            "LEFT JOIN sys_product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId}")
    List<Map<String, Object>> selectItemsByOrderId(@Param("orderId") Long orderId);
    
    @Delete("DELETE FROM sys_order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
}