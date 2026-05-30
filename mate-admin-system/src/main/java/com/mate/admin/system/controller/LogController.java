package com.mate.admin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mate.admin.api.common.Result;
import com.mate.admin.system.entity.SysLog;
import com.mate.admin.system.service.SysLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/system/log")
public class LogController {

    @Resource
    private SysLogService logService;

    @GetMapping("/page")
    public Result<Page<SysLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.eq(SysLog::getUsername, username);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return Result.ok(logService.page(new Page<>(pageNum, pageSize), wrapper));
    }
}
