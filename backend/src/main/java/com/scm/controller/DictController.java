package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Dict;
import com.scm.entity.DictItem;
import com.scm.service.DictItemService;
import com.scm.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dict")
@CacheConfig(cacheNames = "dict")
public class DictController {
    
    @Autowired
    private DictService dictService;
    
    @Autowired
    private DictItemService dictItemService;
    
    @GetMapping("/list")
    public Result<IPage<Dict>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Dict> page = new Page<>(pageNum, pageSize);
            return Result.success(dictService.lambdaQuery().page(page));
        } catch (Exception e) {
            log.error("获取字典列表失败", e);
            return Result.error("获取字典列表失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/{dictId}/items")
    @Cacheable(key = "#dictId")
    public Result<List<DictItem>> getDictItems(@PathVariable Long dictId) {
        try {
            // 字典项管理界面使用，返回所有字典项（包括禁用）
            List<DictItem> dictItems = dictItemService.getAllByDictId(dictId);
            return Result.success(dictItems);
        } catch (Exception e) {
            log.error("获取字典项列表失败", e);
            return Result.error("获取字典项列表失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/code/{dictCode}/items")
    @Cacheable(key = "'code:' + #dictCode")
    public Result<List<DictItem>> getDictItemsByCode(@PathVariable String dictCode) {
        try {
            List<DictItem> dictItems = dictItemService.getByDictCode(dictCode);
            return Result.success(dictItems);
        } catch (Exception e) {
            log.error("根据字典编码获取字典项列表失败", e);
            return Result.error("根据字典编码获取字典项列表失败：" + e.getMessage());
        }
    }
    
    @PostMapping
    @CacheEvict(allEntries = true)
    public Result<Boolean> add(@RequestBody Dict dict) {
        try {
            return Result.success(dictService.save(dict));
        } catch (Exception e) {
            log.error("添加字典失败", e);
            return Result.error("添加字典失败：" + e.getMessage());
        }
    }
    
    @PutMapping
    @CacheEvict(allEntries = true)
    public Result<Boolean> update(@RequestBody Dict dict) {
        try {
            return Result.success(dictService.updateById(dict));
        } catch (Exception e) {
            log.error("更新字典失败", e);
            return Result.error("更新字典失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @CacheEvict(allEntries = true)
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            // 级联删除字典项
            dictItemService.deleteByDictId(id);
            // 删除字典
            return Result.success(dictService.removeById(id));
        } catch (Exception e) {
            log.error("删除字典失败", e);
            return Result.error("删除字典失败：" + e.getMessage());
        }
    }
}