package com.mate.admin.system.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.api.feign.UaaFeignClient;
import com.mate.admin.system.aspect.Idempotent;
import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
public class UserController {

    @Resource
    private SysUserService userService;
    @Resource
    private UaaFeignClient uaaFeignClient;

    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(userService.pageQuery(pageNum, pageSize, deptId, status));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @Idempotent
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        userService.addUser(user);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    /** 跨服务调用 UAA 校验用户是否存在 */
    @GetMapping("/{id}/checkExists")
    public Result<Boolean> checkExists(@PathVariable Long id) {
        return uaaFeignClient.checkUserExists(id);
    }
}
