package com.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Material;

import java.util.List;
import java.util.Map;

/**
 * 物料Service接口
 */
public interface MaterialService extends IService<Material> {
    /**
     * 分页查询物料列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Material> pageMaterials(Integer page, Integer pageSize, Map<String, Object> params);

    /**
     * 根据ID获取物料详情
     * @param id 物料ID
     * @return 物料信息
     */
    Material getMaterialById(Long id);

    /**
     * 创建物料
     * @param material 物料信息
     * @return 是否成功
     */
    boolean createMaterial(Material material);

    /**
     * 更新物料
     * @param material 物料信息
     * @return 是否成功
     */
    boolean updateMaterial(Material material);

    /**
     * 更新物料状态
     * @param id 物料ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateMaterialStatus(Long id, Integer status);

    /**
     * 删除物料
     * @param id 物料ID
     * @return 是否成功
     */
    boolean deleteMaterial(Long id);

    /**
     * 批量删除物料
     * @param ids 物料ID列表
     * @return 是否成功
     */
    boolean batchDeleteMaterials(List<Long> ids);

    /**
     * 根据物料类型获取物料列表
     * @param materialType 物料类型
     * @return 物料列表
     */
    List<Material> getMaterialsByType(Integer materialType);

    /**
     * 检查物料编码是否存在
     * @param materialCode 物料编码
     * @param excludeId 排除的ID（用于编辑时）
     * @return 是否存在
     */
    boolean isMaterialCodeExists(String materialCode, Long excludeId);
}