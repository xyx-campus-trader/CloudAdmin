package com.mate.admin.system.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.system.aspect.IdempotentAspect;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 幂等性 Token 管理
 */
@RestController
@RequestMapping("/system")
public class IdempotentController {

    @Resource
    private IdempotentAspect idempotentAspect;

    @GetMapping("/idempotent/token")
    public Result<String> getToken() {
        return Result.ok(idempotentAspect.generateToken());
    }
}
