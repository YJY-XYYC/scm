package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.dto.SalesDTO;
import com.scm.dto.SalesStatisticsDTO.StatisticsData;
import com.scm.entity.Sales;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper extends BaseMapper<Sales> {

    @Select({
        "<script>",
        "SELECT",
        "    COALESCE(SUM(total_amount), 0) as totalSales,",
        "    COUNT(*) as orderCount,",
        "    CASE WHEN COUNT(*) > 0 THEN COALESCE(SUM(total_amount)/COUNT(*), 0) ELSE 0 END as averageAmount",
        "FROM sys_order",
        "WHERE status > 0",
        "<if test='startDate != null and startDate != \"\"'>",
        "    AND DATE(create_time) >= #{startDate}",
        "</if>",
        "<if test='endDate != null and endDate != \"\"'>",
        "    AND DATE(create_time) &lt;= #{endDate}",
        "</if>",
        "</script>"
    })
    StatisticsData selectStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select({
        "<script>",
        "SELECT",
        "    date,",
        "    orderCount,",
        "    totalAmount,",
        "    averageAmount",
        "FROM (",
        "    SELECT",
        "        DATE_FORMAT(DATE(create_time), '%Y-%m-%d') AS date,",
        "        COUNT(*) AS orderCount,",
        "        COALESCE(SUM(total_amount), 0) AS totalAmount,",
        "        CASE",
        "            WHEN COUNT(*) > 0 THEN COALESCE(SUM(total_amount) / COUNT(*), 0)",
        "            ELSE 0",
        "        END AS averageAmount",
        "    FROM sys_order",
        "    WHERE status > 0",
        "    <if test='startDate != null and startDate != \"\"'>",
        "        AND DATE(create_time) >= #{startDate}",
        "    </if>",
        "    <if test='endDate != null and endDate != \"\"'>",
        "        AND DATE(create_time) &lt;= #{endDate}",
        "    </if>",
        "    GROUP BY DATE_FORMAT(DATE(create_time), '%Y-%m-%d')",
        ") t",
        "ORDER BY date DESC",
        "</script>"
    })
    IPage<SalesDTO> selectSalesList(Page<SalesDTO> page,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    @Select({
        "SELECT COUNT(*)",
        "FROM (",
        "    SELECT",
        "        DATE_FORMAT(DATE(create_time), '%Y-%m-%d') AS date",
        "    FROM sys_order",
        "    WHERE status > 0",
        "    GROUP BY DATE_FORMAT(DATE(create_time), '%Y-%m-%d')",
        ") t"
    })
    Integer selectSalesCount();
} 