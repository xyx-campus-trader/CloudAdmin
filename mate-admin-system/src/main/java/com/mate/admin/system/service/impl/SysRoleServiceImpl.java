package com.mate.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysRole;
import com.mate.admin.system.mapper.SysRoleMapper;
import com.mate.admin.system.mapper.SysMenuMapper;
import com.mate.admin.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private SysMenuMapper menuMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:assign:role:";
    private static final long LOCK_TTL_SECONDS = 10;

    /**
     * 安全释放锁的 Lua 脚本：仅当 value 匹配时才删除，防止误删他人持有的锁
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end",
            Long.class);

    /**
     * 分配角色权限（SETNX 分布式锁防并发覆盖）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        String lockKey = LOCK_PREFIX + roleId;
        String lockVal = UUID.randomUUID().toString();

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockVal, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new RuntimeException("当前操作繁忙，请稍后重试");
        }

        try {
            roleMapper.deleteRoleMenus(roleId);
            if (menuIds != null && !menuIds.isEmpty()) {
                roleMapper.insertRoleMenus(roleId, menuIds);
            }
            log.info("角色 {} 权限分配成功，菜单数: {}",
                    roleId, menuIds == null ? 0 : menuIds.size());
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey), lockVal);
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }
}
