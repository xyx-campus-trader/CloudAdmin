package com.mate.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mate.admin.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    /** 菜单树（缓存防三穿） */
    List<SysMenu> getMenuTree(Long userId);

    /** 更新后删除缓存 */
    SysMenu updateMenu(SysMenu menu);
}
