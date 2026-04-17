package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Material;
import com.scm.mapper.MaterialMapper;
import com.scm.service.MaterialService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物料Service实现类
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    @Override
    public Page<Material> pageMaterials(Integer page, Integer pageSize, Map<String, Object> params) {
        Page<Material> materialPage = new Page<>(page, pageSize);
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();

        // 设置查询条件
        if (params != null) {
            // 物料编码和名称模糊查询（使用or条件连接）
            if ((params.containsKey("materialCode") && params.get("materialCode") != null && !params.get("materialCode").toString().isEmpty()) ||
                (params.containsKey("materialName") && params.get("materialName") != null && !params.get("materialName").toString().isEmpty())) {
                queryWrapper.and(qw -> {
                    if (params.containsKey("materialCode") && params.get("materialCode") != null && !params.get("materialCode").toString().isEmpty()) {
                        qw.like("material_code", params.get("materialCode"));
                    }
                    if (params.containsKey("materialName") && params.get("materialName") != null && !params.get("materialName").toString().isEmpty()) {
                        qw.or().like("material_name", params.get("materialName"));
                    }
                });
            }
            // 物料类型精确查询
            if (params.containsKey("materialType") && params.get("materialType") != null && !params.get("materialType").toString().isEmpty()) {
                queryWrapper.eq("material_type", params.get("materialType"));
            }
            // 状态精确查询
            if (params.containsKey("status") && params.get("status") != null && !params.get("status").toString().isEmpty()) {
                queryWrapper.eq("status", params.get("status"));
            }
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc("create_time");

        return baseMapper.selectPage(materialPage, queryWrapper);
    }

    @Override
    public Material getMaterialById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean createMaterial(Material material) {
        // 设置创建和更新时间
        material.setCreateTime(new Date());
        material.setUpdateTime(new Date());
        return baseMapper.insert(material) > 0;
    }

    @Override
    public boolean updateMaterial(Material material) {
        // 更新时间
        material.setUpdateTime(new Date());
        return baseMapper.updateById(material) > 0;
    }

    @Override
    public boolean updateMaterialStatus(Long id, Integer status) {
        Material material = new Material();
        material.setId(id);
        material.setStatus(status);
        material.setUpdateTime(new Date());
        return baseMapper.updateById(material) > 0;
    }

    @Override
    public boolean deleteMaterial(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchDeleteMaterials(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public List<Material> getMaterialsByType(Integer materialType) {
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("material_type", materialType);
        queryWrapper.eq("status", 1); // 只查询启用状态的物料
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean isMaterialCodeExists(String materialCode, Long excludeId) {
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("material_code", materialCode);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return baseMapper.selectCount(queryWrapper) > 0;
    }
}