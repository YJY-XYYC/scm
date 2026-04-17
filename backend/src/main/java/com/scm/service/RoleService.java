package com.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    
    /**
     * 分配菜单权限
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
    
    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
    
    /**
     * 检查角色是否被使用
     */
    boolean checkRoleUsed(Long roleId);
    
    /**
     * 获取所有可用角色
     */
    List<Role> getAllActiveRoles();
}