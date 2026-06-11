package com.mate.admin.system.config;

import com.mate.admin.api.common.Result;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Feign 调用失败 —— 跨服务通信异常，本地事务已回滚 */
    @ExceptionHandler(FeignException.class)
    public Result<Void> handleFeign(FeignException e) {
        log.error("Feign 调用失败, status={}, message={}", e.status(), e.getMessage(), e);
        return Result.fail(503, "跨服务调用失败，请稍后重试");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        String msg = e.getMessage();
        if (msg != null && msg.contains("uk_username")) {
            return Result.fail("用户名已存在");
        }
        if (msg != null && msg.contains("uk_role_code")) {
            return Result.fail("角色编码已存在");
        }
        return Result.fail("数据重复，请检查后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
