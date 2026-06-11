package com.mate.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysRole;
import com.mate.admin.system.mapper.SysRoleMapper;
import com.mate.admin.system.mapper.SysMenuMapper;
import com.mate.admin.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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
    private RedissonClient redissonClient;

    /**
     * 分配角色权限（Redisson 分布式锁 + 事务保证删除插入原子性）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        String lockKey = "lock:assign:role:" + roleId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("当前操作繁忙，请稍后重试");
            }

            roleMapper.deleteRoleMenus(roleId);
            if (menuIds != null && !menuIds.isEmpty()) {
                roleMapper.insertRoleMenus(roleId, menuIds);
            }

            log.info("角色 {} 权限分配成功，菜单数: {}",
                    roleId, menuIds == null ? 0 : menuIds.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }
}
