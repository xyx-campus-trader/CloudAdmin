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
}
