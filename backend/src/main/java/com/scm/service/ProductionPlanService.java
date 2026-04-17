package com.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.entity.ProductionPlan;
import java.util.Map;

public interface ProductionPlanService extends IService<ProductionPlan> {
    
    /**
     * 获取生产计划列表
     * @param params 查询参数
     * @return 分页列表
     */
    Page<ProductionPlan> getProductionPlans(Map<String, Object> params);
    
    /**
     * 创建生产计划
     * @param productionPlan 生产计划信息
     * @return 创建结果
     */
    boolean createProductionPlan(ProductionPlan productionPlan);
    
    /**
     * 更新生产计划
     * @param productionPlan 生产计划信息
     * @return 更新结果
     */
    boolean updateProductionPlan(ProductionPlan productionPlan);
    
    /**
     * 删除生产计划
     * @param id 生产计划ID
     * @return 删除结果
     */
    boolean deleteProductionPlan(Long id);
    
    /**
     * 更新生产计划状态
     * @param id 生产计划ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean updateProductionPlanStatus(Long id, Integer status);
}
