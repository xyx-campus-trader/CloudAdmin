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
     * 角色分配权限
     * Redisson 可重入锁防并发冲突，看门狗自动续期
     * 锁粒度：角色ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        RLock lock = redissonClient.getLock("lock:role:" + roleId);
        try {
            // tryLock(3, 10, SECONDS): 最多等3秒, 锁10秒自动释放, 看门狗在10秒内自动续期
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new RuntimeException("操作冲突，请稍后重试");
            }
            roleMapper.deleteRoleMenus(roleId);
            if (menuIds != null && !menuIds.isEmpty()) {
                roleMapper.insertRoleMenus(roleId, menuIds);
            }
            log.info("角色权限分配成功, roleId: {}", roleId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("角色权限分配中断");
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
