package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Dict;
import com.scm.entity.DictItem;
import com.scm.mapper.DictItemMapper;
import com.scm.service.DictItemService;
import com.scm.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {
    
    @Autowired
    private DictService dictService;
    
    @Override
    public List<DictItem> getByDictId(Long dictId) {
        return baseMapper.selectByDictId(dictId);
    }
    
    @Override
    public List<DictItem> getAllByDictId(Long dictId) {
        return baseMapper.selectAllByDictId(dictId);
    }
    
    @Override
    public List<DictItem> getByDictCode(String dictCode) {
        Dict dict = dictService.getByDictCode(dictCode);
        if (dict == null) {
            return Collections.emptyList();
        }
        return getByDictId(dict.getId());
    }
    
    @Override
    public boolean deleteByDictId(Long dictId) {
        baseMapper.deleteByDictId(dictId);
        return true;
    }
    
    @Override
    public boolean checkLabelExists(Long dictId, String label, Long excludeId) {
        QueryWrapper<DictItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_id", dictId)
                   .eq("label", label);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return count(queryWrapper) > 0;
    }
    
    @Override
    public boolean checkValueExists(Long dictId, String value, Long excludeId) {
        QueryWrapper<DictItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_id", dictId)
                   .eq("value", value);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return count(queryWrapper) > 0;
    }
}