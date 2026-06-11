package com.mate.admin.system.aspect;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验切面
 * 前端先 GET /system/idempotent/token 获取 Token → 存 Redis
 * 提交时 Header 携带 X-Idempotent-Token → 切面校验并删除
 * 同一 Token 仅消费一次，防重复提交
 */
@Slf4j
@Aspect
@Component
public class IdempotentAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_PREFIX = "idempotent:";

    @Around("@annotation(com.mate.admin.system.aspect.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 从请求头提取幂等 Token
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }
        String token = attrs.getRequest().getHeader("X-Idempotent-Token");
        if (token == null) {
            throw new RuntimeException("幂等性Token缺失");
        }

        // Redis 删除 Token（原子操作），删成功即首次提交
        String redisKey = TOKEN_PREFIX + token;
        Boolean deleted = stringRedisTemplate.delete(redisKey);
        if (Boolean.FALSE.equals(deleted)) {
            throw new RuntimeException("请勿重复提交");
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 业务失败，恢复 Token 以便用户用同一 Token 重试
            try {
                stringRedisTemplate.opsForValue().set(redisKey, "1", 5, TimeUnit.MINUTES);
            } catch (Exception redisEx) {
                log.error("Token 恢复失败，key={}", redisKey, redisEx);
            }
            throw e;
        }
    }

    /**
     * 生成幂等性 Token 存 Redis（5分钟有效）
     */
    public String generateToken() {
        String token = "idt_" + SecureUtil.md5(String.valueOf(System.nanoTime()));
        stringRedisTemplate.opsForValue().set(TOKEN_PREFIX + token, "1", 5, TimeUnit.MINUTES);
        return token;
    }
}
