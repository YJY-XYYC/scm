package com.scm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Menu;
import com.scm.entity.Role;
import com.scm.entity.User;
import com.scm.service.FileService;
import com.scm.service.MenuService;
import com.scm.service.RoleService;
import com.scm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping({"/user", "/api/user"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileService fileService;
    
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<User> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(username)) {
                wrapper.like(User::getUsername, username);
            }
            return Result.success(userService.page(page, wrapper));
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }
    
    @PostMapping
    public Result<Boolean> add(@RequestBody User user) {
        try {
            // 检查用户名是否已存在（不包括已删除的用户）
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, user.getUsername())
                   .eq(User::getDeleted, 0);
            User existingUser = userService.getOne(wrapper);
            if (existingUser != null) {
                return Result.error("用户名已存在，请更换其他用户名");
            }
            
            return Result.success(userService.save(user));
        } catch (Exception e) {
            log.error("添加用户失败", e);
            // 优化错误信息展示，提供更友好的提示
            String errorMsg = "添加用户失败，请稍后重试";
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                errorMsg = "用户名已存在，请更换其他用户名";
            }
            return Result.error(errorMsg);
        }
    }
    
    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        try {
            return Result.success(userService.updateById(user));
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(userService.removeById(id));
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }
    
    /**
     * 修改用户密码
     */
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.valueOf(params.get("id"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            
            if (userId == null || oldPassword == null || newPassword == null) {
                return Result.error("参数不完整");
            }
            
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 验证旧密码
            if (!user.getPassword().equals(oldPassword)) {
                return Result.error("原密码错误");
            }
            
            // 更新密码
            user.setPassword(newPassword);
            boolean result = userService.updateById(user);
            
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("密码更新失败");
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("操作失败，请稍后重试");
        }
    }
    
    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的头像文件");
        }
        
        // 检查文件类型是否为图片
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("请上传图片类型的文件");
        }
        
        try {
            // 上传文件到文件系统
            String avatarUrl = fileService.uploadFile(file);
            
            // 尝试从SecurityContext获取当前用户，如果用户已登录则更新头像
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                try {
                    // 先尝试获取用户名方式
                    String username = authentication.getName();
                    User user = userService.getByUsername(username);
                    if (user != null) {
                        // 更新用户头像路径
                        user.setAvatar(avatarUrl);
                        userService.updateById(user);
                        log.info("用户{}头像上传成功", username);
                    }
                } catch (Exception ex) {
                    log.warn("无法更新用户头像信息: {}", ex.getMessage());
                    // 不影响头像上传结果
                }
            }
            // 如果用户未登录，仍然返回上传成功的URL路径，供注册表单使用
            
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestParam String username) {
        try {
            // 根据用户名获取用户信息
            User user = userService.getByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 构建返回数据，包含sys_user表中所有必要字段
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("avatar", user.getAvatar());
            data.put("role", user.getRole());
            data.put("status", user.getStatus());
            
            // 根据角色设置权限列表
            List<String> roles = new ArrayList<>();
            roles.add(user.getRole());
            data.put("roles", roles);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private MenuService menuService;
    
    /**
     * 获取用户菜单权限信息
     */
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getUserPermissions(@RequestParam String username) {
        try {
            // 根据用户名获取用户信息
            User user = userService.getByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            List<String> permissions = new ArrayList<>();
            List<Map<String, Object>> menus = new ArrayList<>();
            
            // 1. 根据用户角色查询对应的Role实体
            Role role = roleService.getOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getName, user.getRole())
                    .or().eq(Role::getCode, user.getRole())
                    .eq(Role::getStatus, 1));
            
            if (role != null) {
                // 2. 获取该角色拥有的菜单ID列表
                List<Long> menuIds = roleService.getMenuIdsByRoleId(role.getId());
                
                if (!menuIds.isEmpty()) {
                    // 3. 获取所有菜单的树形结构
                    List<Menu> allMenus = menuService.getMenuTree();
                    
                    // 4. 根据菜单ID过滤出用户有权限的菜单
                    List<Menu> permissionMenus = filterPermissionMenus(allMenus, menuIds);
                    
                    // 5. 将Menu实体转换为前端需要的菜单格式
                    menus = convertToFrontendMenus(permissionMenus);
                    
                    // 6. 根据菜单ID生成权限列表
                    permissions = generatePermissions(menuIds);
                }
            } else {
                // 如果找不到对应的角色记录，使用默认的菜单和权限（首页）
                Map<String, Object> dashboardMenu = new HashMap<>();
                dashboardMenu.put("index", "/dashboard");
                dashboardMenu.put("label", "首页");
                dashboardMenu.put("icon", "House");
                dashboardMenu.put("isSubMenu", false);
                menus.add(dashboardMenu);
                permissions.add("dashboard:view");
            }
            
            result.put("permissions", permissions);
            result.put("menus", menus);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Result.error("获取用户权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据菜单ID列表过滤出用户有权限的菜单
     */
    private List<Menu> filterPermissionMenus(List<Menu> allMenus, List<Long> menuIds) {
        // 将菜单ID列表转换为Set以提高查找效率
        Set<Long> menuIdSet = new HashSet<>(menuIds);
        List<Menu> permissionMenus = new ArrayList<>();
        
        // 递归过滤菜单树
        filterMenuTree(allMenus, menuIdSet, permissionMenus);
        
        return permissionMenus;
    }
    
    /**
     * 递归过滤菜单树
     */
    private void filterMenuTree(List<Menu> menus, Set<Long> menuIdSet, List<Menu> result) {
        for (Menu menu : menus) {
            // 如果菜单ID在权限列表中
            if (menuIdSet.contains(menu.getId())) {
                // 创建新的菜单对象，避免修改原菜单树
                Menu newMenu = new Menu();
                newMenu.setId(menu.getId());
                newMenu.setName(menu.getName());
                newMenu.setPath(menu.getPath());
                newMenu.setComponent(menu.getComponent());
                newMenu.setIcon(menu.getIcon());
                newMenu.setParentId(menu.getParentId());
                newMenu.setSort(menu.getSort());
                newMenu.setHidden(menu.getHidden());
                newMenu.setChildren(new ArrayList<>());
                
                // 如果有子菜单，递归过滤
                if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                    filterMenuTree(menu.getChildren(), menuIdSet, newMenu.getChildren());
                }
                
                result.add(newMenu);
            }
        }
    }
    
    /**
     * 将Menu实体转换为前端需要的菜单格式
     */
    private List<Map<String, Object>> convertToFrontendMenus(List<Menu> menus) {
        List<Map<String, Object>> frontendMenus = new ArrayList<>();
        
        for (Menu menu : menus) {
            Map<String, Object> frontendMenu = new HashMap<>();
            frontendMenu.put("index", menu.getPath());
            frontendMenu.put("label", menu.getName());
            frontendMenu.put("icon", menu.getIcon());
            
            // 检查是否有子菜单
            boolean hasChildren = menu.getChildren() != null && !menu.getChildren().isEmpty();
            frontendMenu.put("isSubMenu", hasChildren);
            
            // 如果有子菜单，递归转换
            if (hasChildren) {
                List<Map<String, Object>> children = convertToFrontendMenus(menu.getChildren());
                frontendMenu.put("children", children);
            }
            
            frontendMenus.add(frontendMenu);
        }
        
        return frontendMenus;
    }
    
    /**
     * 根据菜单ID生成权限列表
     */
    private List<String> generatePermissions(List<Long> menuIds) {
        // 这里可以根据实际需求从数据库中查询每个菜单对应的权限码
        // 目前根据菜单ID生成基础的查看权限
        List<String> permissions = new ArrayList<>();
        
        // 添加基础的首页权限
        permissions.add("dashboard:view");
        
        // 根据菜单ID添加对应的权限
        // 这里可以从menu表中查询每个菜单的权限码，或者根据路径生成
        menuIds.forEach(menuId -> {
            // 简单示例，实际应该从数据库查询
            permissions.add("menu:" + menuId + ":view");
        });
        
        return permissions;
    }
}