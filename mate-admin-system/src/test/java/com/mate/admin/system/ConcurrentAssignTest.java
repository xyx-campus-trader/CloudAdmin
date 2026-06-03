package com.mate.admin.system;

import com.mate.admin.system.service.SysRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class ConcurrentAssignTest {

    @Resource
    private SysRoleService roleService;

    @Test
    void testConcurrentAssign() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 管理员A：给角色1分配菜单[1,2,3]
        Thread adminA = new Thread(() -> {
            try {
                latch.await(); // 等B也准备好
                roleService.assignMenus(1L, Arrays.asList(1L, 2L, 3L));
                System.out.println("管理员A 完成");
            } catch (Exception e) {
                System.out.println("管理员A 失败: " + e.getMessage());
            }
        });

        // 管理员B：给角色1分配菜单[4,5,6]
        Thread adminB = new Thread(() -> {
            try {
                latch.await(); // 等A也准备好
                roleService.assignMenus(1L, Arrays.asList(4L, 5L, 6L));
                System.out.println("管理员B 完成");
            } catch (Exception e) {
                System.out.println("管理员B 失败: " + e.getMessage());
            }
        });

        adminA.start();
        adminB.start();

        Thread.sleep(100); // 等两个线程都到起跑线
        latch.countDown(); // 同时出发！

        adminA.join();
        adminB.join();

        // 看结果
        System.out.println("最终角色1的菜单: " + roleService.getMenuIds(1L));
    }
}