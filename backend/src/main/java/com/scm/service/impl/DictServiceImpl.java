package com.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Dict;
import com.scm.mapper.DictMapper;
import com.scm.service.DictService;
import org.springframework.stereotype.Service;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Override
    public Dict getByDictCode(String dictCode) {
        return lambdaQuery().eq(Dict::getDictCode, dictCode).eq(Dict::getStatus, 1).one();
    }
} 