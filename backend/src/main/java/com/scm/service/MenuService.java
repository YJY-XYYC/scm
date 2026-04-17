package com.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Menu;
import java.util.List;

public interface MenuService extends IService<Menu> {
    
    /**
     * 获取菜单列表
     */
    List<Menu> list();
    
    /**
     * 获取树形结构菜单
     */
    List<Menu> getMenuTree();
}