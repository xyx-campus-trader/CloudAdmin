package com.mate.admin.uaa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mate.admin.uaa.entity.User;
import com.mate.admin.uaa.mapper.UserMapper;
import com.mate.admin.uaa.service.AuthService;
import com.mate.admin.uaa.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已禁用");
        }
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        // Token 存 Redis，支持主动踢出
        redisTemplate.opsForValue().set("token:" + user.getId(), token,
                jwtUtils.getClaims(token).getExpiration().getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
        log.info("用户登录成功: {}", username);
        return token;
    }

    @Override
    public void logout(Long userId) {
        redisTemplate.delete("token:" + userId);
        log.info("用户登出, userId: {}", userId);
    }

    @Override
    public boolean userExists(Long userId) {
        return userMapper.selectById(userId) != null;
    }

    /**
     * UAA 为新用户初始化认证资源。
     * 若此处失败抛异常，System 服务收到 Feign 异常后会触发本地事务回滚。
     */
    @Override
    public void syncUserAuth(Long userId, String username) {
        if (userId == null || username == null || username.isBlank()) {
            throw new RuntimeException("用户认证同步参数非法");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在，无法同步认证信息");
        }
        // 在 Redis 中预置一个占位 token，防止首次登录时缓存穿透
        // 实际场景可以是：初始化会话槽位、写入白名单、同步到 SSO 等
        redisTemplate.opsForValue().set("user:auth:" + userId, username,
                30, TimeUnit.MINUTES);
        log.info("UAA 认证资源初始化成功, userId: {}, username: {}", userId, username);
    }
}
