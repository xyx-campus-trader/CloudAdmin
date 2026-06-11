package com.mate.admin.system.controller;

import com.mate.admin.api.common.Result;
import com.mate.admin.system.entity.SysMenu;
import com.mate.admin.system.service.SysMenuService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Resource
    private SysMenuService menuService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String MENU_TREE_KEY = "menu:tree";

    /**
     * 菜单树（Cache-Aside + 互斥锁防击穿 + 空值防穿透 + 随机过期防雪崩）
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree(@RequestParam(required = false) Long userId) {
        return Result.ok(menuService.getMenuTree(userId));
    }

    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.ok(menuService.list());
    }

    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(menuService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        stringRedisTemplate.delete(MENU_TREE_KEY);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        stringRedisTemplate.delete(MENU_TREE_KEY);
        return Result.ok();
    }
}
