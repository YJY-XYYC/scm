package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Menu;
import com.scm.mapper.MenuMapper;
import com.scm.service.MenuService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    
    @Override
    public List<Menu> list() {
        // 使用select方法明确指定要查询的字段，避免查询不存在的children字段
        return baseMapper.selectList(new LambdaQueryWrapper<Menu>()
                .select(Menu::getId, Menu::getName, Menu::getPath, Menu::getComponent, 
                        Menu::getIcon, Menu::getParentId, Menu::getSort, Menu::getHidden)
                .orderByAsc(Menu::getParentId)
                .orderByAsc(Menu::getSort));
    }
    
    @Override
    public List<Menu> getMenuTree() {
        // 获取所有菜单
        List<Menu> allMenus = this.list();
        
        // 构建树形结构
        return buildMenuTree(allMenus);
    }
    
    /**
     * 构建菜单树
     * @param menus 所有菜单列表
     * @return 树形结构菜单
     */
    private List<Menu> buildMenuTree(List<Menu> menus) {
        // 转换为Map便于查找父菜单
        Map<Long, Menu> menuMap = new HashMap<>();
        List<Menu> rootMenus = new ArrayList<>();
        
        // 为每个菜单创建children列表并加入Map
        for (Menu menu : menus) {
            // 动态添加children属性，用于构建树形结构
            menu.setChildren(new ArrayList<>());
            menuMap.put(menu.getId(), menu);
            
            // 根菜单(parentId为0或null)
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
            }
        }
        
        // 构建树形结构
        for (Menu menu : menus) {
            // 非根菜单，添加到父菜单的children列表
            if (menu.getParentId() != null && menu.getParentId() != 0) {
                Menu parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                }
            }
        }
        
        return rootMenus;
    }
}