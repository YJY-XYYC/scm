package com.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.DictItem;

import java.util.List;

public interface DictItemService extends IService<DictItem> {
    
    /**
     * 根据字典ID获取字典项列表（仅启用）
     */
    List<DictItem> getByDictId(Long dictId);
    
    /**
     * 根据字典ID获取所有字典项（包括禁用）
     */
    List<DictItem> getAllByDictId(Long dictId);
    
    /**
     * 根据字典编码获取字典项列表
     */
    List<DictItem> getByDictCode(String dictCode);
    
    /**
     * 删除字典时级联删除字典项
     */
    boolean deleteByDictId(Long dictId);
    
    /**
     * 检查字典项名称是否已存在
     */
    boolean checkLabelExists(Long dictId, String label, Long excludeId);
    
    /**
     * 检查字典项值是否已存在
     */
    boolean checkValueExists(Long dictId, String value, Long excludeId);
}