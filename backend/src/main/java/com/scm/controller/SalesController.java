package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.dto.SalesDTO;
import com.scm.dto.SalesStatisticsDTO;
import com.scm.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    @GetMapping("/statistics")
    public Result<SalesStatisticsDTO> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            SalesStatisticsDTO statistics = salesService.getStatistics(startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取销售统计数据失败", e);
            return Result.error("获取销售统计数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<IPage<SalesDTO>> getSalesList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<SalesDTO> page = new Page<>(pageNum, pageSize);
            return Result.success(salesService.getSalesList(page, startDate, endDate));
        } catch (Exception e) {
            log.error("获取销售明细失败", e);
            return Result.error("获取销售明细失败：" + e.getMessage());
        }
    }
}