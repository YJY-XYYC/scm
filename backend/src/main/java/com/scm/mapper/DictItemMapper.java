package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scm.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {
    
    /**
     * 根据字典ID获取字典项列表（仅启用）
     */
    List<DictItem> selectByDictId(Long dictId);
    
    /**
     * 根据字典ID获取所有字典项（包括禁用）
     */
    List<DictItem> selectAllByDictId(Long dictId);
    
    /**
     * 删除指定字典ID的所有字典项
     */
    void deleteByDictId(Long dictId);
}