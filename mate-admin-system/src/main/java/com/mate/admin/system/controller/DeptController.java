package com.mate.admin.system.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.system.entity.SysDept;
import com.mate.admin.system.service.SysDeptService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/dept")
public class DeptController {

    @Resource
    private SysDeptService deptService;

    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.list());
    }

    @GetMapping("/{id}")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        deptService.updateById(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.ok();
    }
}
