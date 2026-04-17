package com.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.dto.SalesDTO;
import com.scm.dto.SalesStatisticsDTO;
import com.scm.entity.Sales;
import com.scm.mapper.SalesMapper;
import com.scm.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class SalesServiceImpl extends ServiceImpl<SalesMapper, Sales> implements SalesService {

    @Autowired
    private SalesMapper salesMapper;

    @Override
    public SalesStatisticsDTO getStatistics(String startDate, String endDate) {
        try {
            SalesStatisticsDTO dto = new SalesStatisticsDTO();
            
            // 获取当前时间段的统计数据
            SalesStatisticsDTO.StatisticsData currentData = salesMapper.selectStatistics(startDate, endDate);
            dto.setCurrent(currentData);
            
            // 获取上一个时间段的统计数据（用于环比）
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                long days = end.toEpochDay() - start.toEpochDay();
                
                LocalDate compareStart = start.minusDays(days + 1);
                LocalDate compareEnd = end.minusDays(days + 1);
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                SalesStatisticsDTO.StatisticsData compareData = salesMapper.selectStatistics(
                    compareStart.format(formatter),
                    compareEnd.format(formatter)
                );
                dto.setCompare(compareData);
            }
            
            return dto;
        } catch (Exception e) {
            log.error("获取销售统计数据失败", e);
            throw new RuntimeException("获取销售统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<SalesDTO> getSalesList(Page<SalesDTO> page, String startDate, String endDate) {
        return salesMapper.selectSalesList(page, startDate, endDate);
    }
}