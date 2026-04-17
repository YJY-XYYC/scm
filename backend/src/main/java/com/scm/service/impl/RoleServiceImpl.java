package com.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Role;
import com.scm.entity.SysRoleMenu;
import com.scm.entity.SysUserRole;
import com.scm.mapper.RoleMapper;
import com.scm.mapper.SysRoleMenuMapper;
import com.scm.mapper.SysUserRoleMapper;
import com.scm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Override
    @Transactional
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有的菜单权限
        sysRoleMenuMapper.deleteByRoleId(roleId);
        
        // 循环插入新的菜单权限
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateTime(LocalDateTime.now());
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        
        return true;
    }
    
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
    
    @Override
    public boolean checkRoleUsed(Long roleId) {
        // 查询用户-角色关联表中是否存在该角色的使用记录
        return sysUserRoleMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)) > 0;
    }
    
    @Override
    public List<Role> getAllActiveRoles() {
        // 查询所有启用状态的角色
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1));
    }
}