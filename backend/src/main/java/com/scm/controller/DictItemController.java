package com.scm.controller;

import com.scm.common.Result;
import com.scm.entity.DictItem;
import com.scm.service.DictItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dict/item")
public class DictItemController {
    
    @Autowired
    private DictItemService dictItemService;
    
    @PostMapping
    public Result<Boolean> add(@RequestBody DictItem dictItem) {
        try {
            // 检查字典项名称是否已存在
            if (dictItemService.checkLabelExists(dictItem.getDictId(), dictItem.getLabel(), null)) {
                return Result.error("字典项名称已存在");
            }
            // 检查字典项值是否已存在
            if (dictItemService.checkValueExists(dictItem.getDictId(), dictItem.getValue(), null)) {
                return Result.error("字典项值已存在");
            }
            // 设置默认排序
            if (dictItem.getSort() == null) {
                dictItem.setSort(0);
            }
            // 设置默认状态
            if (dictItem.getStatus() == null) {
                dictItem.setStatus(1);
            }
            return Result.success(dictItemService.save(dictItem));
        } catch (Exception e) {
            log.error("添加字典项失败", e);
            return Result.error("添加字典项失败：" + e.getMessage());
        }
    }
    
    @PutMapping
    public Result<Boolean> update(@RequestBody DictItem dictItem) {
        try {
            // 检查字典项名称是否已存在（排除当前项）
            if (dictItemService.checkLabelExists(dictItem.getDictId(), dictItem.getLabel(), dictItem.getId())) {
                return Result.error("字典项名称已存在");
            }
            // 检查字典项值是否已存在（排除当前项）
            if (dictItemService.checkValueExists(dictItem.getDictId(), dictItem.getValue(), dictItem.getId())) {
                return Result.error("字典项值已存在");
            }
            return Result.success(dictItemService.updateById(dictItem));
        } catch (Exception e) {
            log.error("更新字典项失败", e);
            return Result.error("更新字典项失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(dictItemService.removeById(id));
        } catch (Exception e) {
            log.error("删除字典项失败", e);
            return Result.error("删除字典项失败：" + e.getMessage());
        }
    }
}