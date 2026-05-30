package com.mate.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mate.admin.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    void assignMenus(Long roleId, List<Long> menuIds);
    List<Long> getMenuIds(Long roleId);
}
