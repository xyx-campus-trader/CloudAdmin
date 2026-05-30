package com.mate.admin.system.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.api.feign.UaaFeignClient;
import com.mate.admin.system.aspect.Idempotent;
import com.mate.admin.system.entity.SysRole;
import com.mate.admin.system.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Resource
    private SysRoleService roleService;
    @Resource
    private UaaFeignClient uaaFeignClient;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.list());
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @Idempotent
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.ok();
    }

    /**
     * 分配权限（Redisson 分布式锁防并发冲突）
     */
    @PostMapping("/{roleId}/assignMenus")
    public Result<Void> assignMenus(@PathVariable Long roleId,
                                     @RequestBody Map<String, List<Long>> body) {
        roleService.assignMenus(roleId, body.get("menuIds"));
        return Result.ok();
    }

    @GetMapping("/{roleId}/menuIds")
    public Result<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return Result.ok(roleService.getMenuIds(roleId));
    }
}
