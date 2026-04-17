package com.scm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Role;
import com.scm.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<IPage<Role>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Role> page = new Page<>(pageNum, pageSize);
            
            // 构建查询条件
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.like(Role::getName, keyword).or().like(Role::getCode, keyword);
            }
            
            IPage<Role> rolePage = roleService.page(page, wrapper);
            return Result.success(rolePage);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody Role role) {
        try {
            // 设置默认值
            if (role.getStatus() == null) {
                role.setStatus(1); // 默认启用
            }
            return Result.success(roleService.save(role));
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return Result.error("添加角色失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Role role) {
        try {
            return Result.success(roleService.updateById(role));
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error("更新角色失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            // 检查角色是否被使用
            if (roleService.checkRoleUsed(id)) {
                return Result.error("该角色正在被使用，无法删除");
            }
            return Result.success(roleService.removeById(id));
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error("删除角色失败：" + e.getMessage());
        }
    }
    
    /**
     * 分配菜单权限
     */
    @PostMapping("/assignMenus")
    public Result<Boolean> assignMenus(@RequestBody Map<String, Object> params) {
        try {
            Long roleId = Long.valueOf(params.get("roleId").toString());
            
            // 正确处理菜单ID列表，显式转换为Long类型
            List<?> rawMenuIds = (List<?>) params.get("menuIds");
            List<Long> menuIds = new ArrayList<>();
            if (rawMenuIds != null) {
                for (Object id : rawMenuIds) {
                    if (id instanceof Integer) {
                        menuIds.add(((Integer) id).longValue());
                    } else if (id instanceof Long) {
                        menuIds.add((Long) id);
                    } else if (id instanceof String) {
                        menuIds.add(Long.valueOf((String) id));
                    }
                }
            }
            
            return Result.success(roleService.assignMenus(roleId, menuIds));
        } catch (Exception e) {
            log.error("分配菜单权限失败", e);
            return Result.error("分配菜单权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取角色的菜单权限
     */
    @GetMapping("/{roleId}/menuIds")
    public Result<List<Long>> getMenuIds(@PathVariable Long roleId) {
        try {
            List<Long> menuIds = roleService.getMenuIdsByRoleId(roleId);
            return Result.success(menuIds);
        } catch (Exception e) {
            log.error("获取角色菜单权限失败", e);
            return Result.error("获取角色菜单权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有可用角色
     */
    @GetMapping("/all")
    public Result<List<Role>> getAllActiveRoles() {
        try {
            List<Role> roles = roleService.getAllActiveRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败：" + e.getMessage());
        }
    }
}