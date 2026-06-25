package com.mate.admin.system;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class DataGeneratorTest extends ServiceImpl<SysUserMapper, SysUser> {

    @Resource
    private SysUserMapper sysUserMapper;

    @Test
    void generateUsers() {
        Random random = new Random();
        int total = 1_500_000;
        int batchSize = 5000;
        String encodedPwd = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh";

        // 删掉之前生成的测试数据（保留 admin）
        sysUserMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .ne(SysUser::getUsername, "admin"));

        for (int i = 0; i < total; i += batchSize) {
            List<SysUser> batch = new ArrayList<>(batchSize);
            for (int j = 0; j < batchSize && i + j < total; j++) {
                int idx = i + j + 1;
                SysUser user = new SysUser();
                user.setUsername("user_" + String.format("%07d", idx));
                user.setPassword(encodedPwd);
                user.setEmail("user" + String.format("%07d", idx) + "@example.com");
                user.setPhone("1" + String.format("%010d", (long) (Math.random() * 9_999_999_999L)));
                user.setStatus(random.nextInt(100) < 95 ? 1 : 0);
                user.setDeptId((long) (random.nextInt(20) + 1));
                user.setCreateTime(LocalDateTime.of(2025, 1, 1, 0, 0)
                        .plusSeconds((long) (Math.random() * 44_640_000)));
                user.setIsDeleted(0);
                batch.add(user);
            }
            saveBatch(batch, batchSize);
            System.out.println("已插入 " + Math.min(i + batchSize, total) + " / " + total);
        }
        System.out.println("插入完成！总共 " + total + " 条");
    }
}
