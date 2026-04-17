package com.scm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.ProductionPlan;
import com.scm.service.ProductionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/scm/api/production-plan")
public class ProductionPlanController {
    
    @Autowired
    private ProductionPlanService productionPlanService;
    
    /**
     * 获取生产计划列表
     * @param params 查询参数
     * @return 分页列表
     */
    @GetMapping("/list")
    public Result<Page<ProductionPlan>> getProductionPlans(@RequestParam Map<String, Object> params) {
        try {
            Page<ProductionPlan> page = productionPlanService.getProductionPlans(params);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取生产计划列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取生产计划详情
     * @param id 生产计划ID
     * @return 生产计划详情
     */
    @GetMapping("/{id}")
    public Result<ProductionPlan> getProductionPlanById(@PathVariable Long id) {
        try {
            ProductionPlan productionPlan = productionPlanService.getById(id);
            if (productionPlan != null) {
                return Result.success(productionPlan);
            } else {
                return Result.error("生产计划不存在");
            }
        } catch (Exception e) {
            return Result.error("获取生产计划详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建生产计划
     * @param productionPlan 生产计划信息
     * @return 创建结果
     */
    @PostMapping
    public Result<String> createProductionPlan(@RequestBody ProductionPlan productionPlan) {
        try {
            boolean success = productionPlanService.createProductionPlan(productionPlan);
            if (success) {
                return Result.success("创建生产计划成功");
            } else {
                return Result.error("创建生产计划失败");
            }
        } catch (Exception e) {
            return Result.error("创建生产计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新生产计划
     * @param productionPlan 生产计划信息
     * @return 更新结果
     */
    @PutMapping
    public Result<String> updateProductionPlan(@RequestBody ProductionPlan productionPlan) {
        try {
            boolean success = productionPlanService.updateProductionPlan(productionPlan);
            if (success) {
                return Result.success("更新生产计划成功");
            } else {
                return Result.error("更新生产计划失败");
            }
        } catch (Exception e) {
            return Result.error("更新生产计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除生产计划
     * @param id 生产计划ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProductionPlan(@PathVariable Long id) {
        try {
            boolean success = productionPlanService.deleteProductionPlan(id);
            if (success) {
                return Result.success("删除生产计划成功");
            } else {
                return Result.error("删除生产计划失败");
            }
        } catch (Exception e) {
            return Result.error("删除生产计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新生产计划状态
     * @param id 生产计划ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    public Result<String> updateProductionPlanStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = productionPlanService.updateProductionPlanStatus(id, status);
            if (success) {
                return Result.success("更新生产计划状态成功");
            } else {
                return Result.error("更新生产计划状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新生产计划状态失败: " + e.getMessage());
        }
    }
}
