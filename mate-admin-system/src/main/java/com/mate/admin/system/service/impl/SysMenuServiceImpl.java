package com.mate.admin.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysMenu;
import com.mate.admin.system.mapper.SysMenuMapper;
import com.mate.admin.system.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单服务 - Cache-Aside + 防击穿/穿透/雪崩
 * 使用 StringRedisTemplate + JSON 序列化避免反序列化类型冲突
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String MENU_TREE_KEY = "menu:tree";
    private static final String MENU_TREE_LOCK = "lock:menu:tree";

    @Override
    public List<SysMenu> getMenuTree(Long userId) {
        String cached = stringRedisTemplate.opsForValue().get(MENU_TREE_KEY);
        if (cached != null) {
            if ("[]".equals(cached)) {
                return Collections.emptyList();
            }
            return JSONUtil.toList(cached, SysMenu.class);
        }

        // 互斥锁防击穿
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(MENU_TREE_LOCK, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            try { Thread.sleep(50); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getMenuTree(userId);
        }

        try {
            // 双检
            cached = stringRedisTemplate.opsForValue().get(MENU_TREE_KEY);
            if (cached != null) {
                if ("[]".equals(cached)) return Collections.emptyList();
                return JSONUtil.toList(cached, SysMenu.class);
            }

            List<SysMenu> allMenus = baseMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 1)
                            .orderByAsc(SysMenu::getSort));
            if (allMenus.isEmpty()) {
                stringRedisTemplate.opsForValue().set(MENU_TREE_KEY, "[]", 5, TimeUnit.MINUTES);
                return Collections.emptyList();
            }

            List<SysMenu> tree = buildTree(allMenus);
            int ttl = 55 + new Random().nextInt(11);
            stringRedisTemplate.opsForValue().set(MENU_TREE_KEY, JSONUtil.toJsonStr(tree),
                    ttl, TimeUnit.MINUTES);
            return tree;
        } finally {
            stringRedisTemplate.delete(MENU_TREE_LOCK);
        }
    }

    @Override
    public SysMenu updateMenu(SysMenu menu) {
        baseMapper.updateById(menu);
        stringRedisTemplate.delete(MENU_TREE_KEY);
        return menu;
    }

    private List<SysMenu> buildTree(List<SysMenu> all) {
        Map<Long, List<SysMenu>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenu> roots = childrenMap.getOrDefault(0L, Collections.emptyList());
        for (SysMenu root : all) {
            root.setChildren(childrenMap.getOrDefault(root.getId(), Collections.emptyList()));
        }
        return roots;
    }
}
