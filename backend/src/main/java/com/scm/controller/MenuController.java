package com.scm.controller;

import com.scm.common.Result;
import com.scm.entity.Menu;
import com.scm.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/list")
    public Result<List<Menu>> list() {
        try {
            // 返回树形结构的菜单数据
            return Result.success(menuService.getMenuTree());
        } catch (Exception e) {
            log.error("获取菜单列表失败", e);
            return Result.error("获取菜单列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody Menu menu) {
        try {
            return Result.success(menuService.save(menu));
        } catch (Exception e) {
            log.error("添加菜单失败", e);
            return Result.error("添加菜单失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Menu menu) {
        try {
            return Result.success(menuService.updateById(menu));
        } catch (Exception e) {
            log.error("更新菜单失败", e);
            return Result.error("更新菜单失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(menuService.removeById(id));
        } catch (Exception e) {
            log.error("删除菜单失败", e);
            return Result.error("删除菜单失败：" + e.getMessage());
        }
    }
}