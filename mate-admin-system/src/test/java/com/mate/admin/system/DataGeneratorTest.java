package com.mate.admin.system;

import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Random;

@SpringBootTest
public class DataGeneratorTest {
    @Resource
    private SysUserMapper sysUserMapper;
    @Test
    void generateUsers() {
        Random random = new Random();
        int total = 150000;
        int batchSize = 1000;
        String encodedPwd = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh";
        sysUserMapper.delete(null);
        for (int i = 0; i < total; i += batchSize) {
            for (int j = 0; j < batchSize; j++) {
                SysUser user = new SysUser();
                user.setUsername("user_" + String.format("%07d", i + j + 1));
                user.setPassword(encodedPwd);
                user.setNickname("用户" + String.format("%07d", i + j + 1));
                user.setEmail("user" + String.format("%07d", i + j + 1) + "@example.com");
                user.setPhone("1" + String.format("%010d", (long) (Math.random() * 9999999999L)));
                user.setStatus(random.nextInt(100) < 95 ? 1 : 0);  // 95%启用
                user.setDeptId((long) (random.nextInt(20) + 1));    // 随机分配到1-20号部门
                user.setCreateTime(LocalDateTime.of(2025, 1, 1, 0, 0)
                        .plusSeconds((long) (Math.random() * 44640000))); // 2025-01-01到2026-05-31之间的随机时间
                user.setIsDeleted(0);

                sysUserMapper.insert(user);
            }
            System.out.println("已插入 " + Math.min(i + batchSize, total) + " / " + total);
        }
        System.out.println("插入完成！总共 " + total + " 条");
    }
}
