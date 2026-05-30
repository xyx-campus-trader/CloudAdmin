package com.mate.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mate.admin.system.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    Map<String, Object> pageQuery(int pageNum, int pageSize, Long deptId, Integer status);
    void addUser(SysUser user);
    void updateUser(SysUser user);
    void deleteUser(Long id);
}
