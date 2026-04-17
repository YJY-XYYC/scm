package com.scm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Material;
import com.scm.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物料管理控制器
 */
@RestController
@RequestMapping("/scm/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 获取物料列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Material>> listMaterials(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam Map<String, Object> params) {
        Page<Material> pageData = materialService.pageMaterials(page, pageSize, params);
        return Result.success(pageData);
    }

    /**
     * 根据ID获取物料详情
     */
    @GetMapping("/{id}")
    public Result<Material> getMaterial(@PathVariable Long id) {
        try {
            Material material = materialService.getMaterialById(id);
            if (material == null) {
                return Result.error("物料不存在");
            }
            return Result.success(material);
        } catch (Exception e) {
            return Result.error("获取物料详情失败");
        }
    }

    /**
     * 创建新物料
     */
    @PostMapping
    public Result<String> createMaterial(@RequestBody Material material) {
        try {
            // 检查物料编码是否已存在
            boolean exists = materialService.isMaterialCodeExists(material.getMaterialCode(), null);
            if (exists) {
                return Result.error("物料编码已存在，请使用其他编码");
            }
            
            boolean success = materialService.createMaterial(material);
            if (success) {
                return Result.success("物料创建成功");
            } else {
                return Result.error("物料创建失败");
            }
        } catch (Exception e) {
            return Result.error("物料创建过程中发生错误");
        }
    }

    /**
     * 更新物料信息
     */
    @PutMapping
    public Result<String> updateMaterial(@RequestBody Material material) {
        try {
            // 检查物料是否存在
            Material existingMaterial = materialService.getMaterialById(material.getId());
            if (existingMaterial == null) {
                return Result.error("物料不存在");
            }
            
            // 检查物料编码是否重复（排除当前物料）
            if (!existingMaterial.getMaterialCode().equals(material.getMaterialCode()) && 
                materialService.isMaterialCodeExists(material.getMaterialCode(), material.getId())) {
                return Result.error("物料编码已存在，请使用其他编码");
            }
            
            boolean success = materialService.updateMaterial(material);
            if (success) {
                return Result.success("物料更新成功");
            } else {
                return Result.error("物料更新失败");
            }
        } catch (Exception e) {
            return Result.error("物料更新过程中发生错误");
        }
    }

    /**
     * 更新物料状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> status) {
        try {
            // 检查物料是否存在
            Material material = materialService.getMaterialById(id);
            if (material == null) {
                return Result.error("物料不存在");
            }
            
            Integer newStatus = status.get("status");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return Result.error("无效的状态值，请使用0(禁用)或1(启用)");
            }
            
            boolean success = materialService.updateMaterialStatus(id, newStatus);
            if (success) {
                return Result.success("物料状态更新成功");
            } else {
                return Result.error("物料状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("物料状态更新过程中发生错误");
        }
    }

    /**
     * 删除物料
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMaterial(@PathVariable Long id) {
        try {
            // 检查物料是否存在
            Material material = materialService.getMaterialById(id);
            if (material == null) {
                return Result.error("物料不存在");
            }
            
            boolean success = materialService.deleteMaterial(id);
            if (success) {
                return Result.success("物料删除成功");
            } else {
                return Result.error("物料删除失败");
            }
        } catch (Exception e) {
            return Result.error("物料删除过程中发生错误");
        }
    }

    /**
     * 批量删除物料
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteMaterials(@RequestBody Map<String, List<Long>> ids) {
        try {
            List<Long> idList = ids.get("ids");
            if (idList == null || idList.isEmpty()) {
                return Result.error("请选择要删除的物料");
            }
            
            boolean success = materialService.batchDeleteMaterials(idList);
            if (success) {
                return Result.success("物料批量删除成功");
            } else {
                return Result.error("物料批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("物料批量删除过程中发生错误");
        }
    }

    /**
     * 根据物料类型获取物料列表
     */
    @GetMapping("/by-type/{type}")
    public Result<List<Material>> getMaterialsByType(@PathVariable Integer type) {
        try {
            List<Material> materials = materialService.getMaterialsByType(type);
            return Result.success(materials);
        } catch (Exception e) {
            return Result.error("获取物料列表失败");
        }
    }
}