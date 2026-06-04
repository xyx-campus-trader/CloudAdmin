package com.mate.admin.uaa.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.api.dto.LoginDTO;
import com.mate.admin.api.dto.UserRegisterDTO;
import com.mate.admin.uaa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO dto) {
        try {
            String token = authService.login(dto.getUsername(), dto.getPassword());
            return Result.ok(token);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            authService.logout(Long.parseLong(userId));
        }
        return Result.ok();
    }

    @GetMapping("/user/{userId}/exists")
    public Result<Boolean> checkUserExists(@PathVariable Long userId) {
        return Result.ok(authService.userExists(userId));
    }

    /** 供 System 服务 Feign 调用：为新用户初始化 UAA 认证资源 */
    @PostMapping("/user/sync")
    public Result<Void> syncUserAuth(@RequestBody UserRegisterDTO dto) {
        authService.syncUserAuth(dto.getUserId(), dto.getUsername());
        return Result.ok();
    }
}
