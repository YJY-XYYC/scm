package com.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Dict;

public interface DictService extends IService<Dict> {
    /**
     * 根据字典编码获取字典
     */
    Dict getByDictCode(String dictCode);
} 