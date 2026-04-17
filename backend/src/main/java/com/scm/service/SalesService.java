package com.scm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.dto.SalesDTO;
import com.scm.dto.SalesStatisticsDTO;
import com.scm.entity.Sales;

public interface SalesService extends IService<Sales> {
    SalesStatisticsDTO getStatistics(String startDate, String endDate);
    IPage<SalesDTO> getSalesList(Page<SalesDTO> page, String startDate, String endDate);
} 