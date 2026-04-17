package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.scm.dto.SalesStatisticsDTO.StatisticsData;
import com.scm.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM sys_order WHERE deleted = 0")
    BigDecimal selectTotalSales();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM sys_order WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end}")
    BigDecimal selectSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count, SUM(total_amount) as amount " +
            "FROM sys_order WHERE deleted = 0 AND create_time >= #{date} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> selectWeeklyTrend(@Param("date") LocalDateTime date);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, COUNT(*) as count, SUM(total_amount) as amount " +
            "FROM sys_order WHERE deleted = 0 AND create_time >= #{date} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m')")
    List<Map<String, Object>> selectMonthlyTrend(@Param("date") LocalDateTime date);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, SUM(total_amount) as amount " +
            "FROM sys_order WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> selectWeeklySalesTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, SUM(total_amount) as amount " +
            "FROM sys_order WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m')")
    List<Map<String, Object>> selectMonthlySalesTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select({"<script>",
        "SELECT p.name, SUM(oi.quantity) as quantity, SUM(oi.price * oi.quantity) as amount ",
        "FROM sys_order o ",
        "JOIN sys_order_item oi ON o.id = oi.order_id ",
        "JOIN sys_product p ON oi.product_id = p.id ",
        "WHERE o.deleted = 0 ",
        "<if test='start != null'>",
        "    AND o.create_time >= #{start}",
        "</if>",
        "<if test='end != null'>",
        "    AND o.create_time &lt;= #{end}",
        "</if>",
        "GROUP BY p.id, p.name ",
        "ORDER BY amount DESC",
        "</script>"})
    List<Map<String, Object>> selectSalesRank(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count " +
            "FROM sys_order WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> selectWeeklyOrderTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, COUNT(*) as count " +
            "FROM sys_order WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end} "+
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m')")
    List<Map<String, Object>> selectMonthlyOrderTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}