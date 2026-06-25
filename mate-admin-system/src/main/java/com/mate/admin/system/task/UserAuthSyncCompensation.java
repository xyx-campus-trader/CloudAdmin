package com.mate.admin.system.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mate.admin.api.dto.UserRegisterDTO;
import com.mate.admin.api.feign.UaaFeignClient;
import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户认证同步补偿任务
 * 每隔 5 分钟扫描 auth_synced = 0 的用户，重新尝试同步至 UAA。
 * 作为 addUser 中"补偿删除失败"的最终回补机制。
 */
@Slf4j
@Component
public class UserAuthSyncCompensation {

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private UaaFeignClient uaaFeignClient;

    /**
     * 每 5 分钟扫描一次，回补 auth_synced = 0 的用户。
     * 每次最多处理 100 条，避免全表扫描拖慢数据库。
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void scanAndCompensate() {
        List<SysUser> unsynced = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getAuthSynced, 0)
                        .last("LIMIT 100"));

        if (unsynced.isEmpty()) {
            return;
        }

        log.info("补偿任务：发现 {} 个待同步用户", unsynced.size());
        int success = 0;
        for (SysUser user : unsynced) {
            try {
                UserRegisterDTO dto = new UserRegisterDTO();
                dto.setUserId(user.getId());
                dto.setUsername(user.getUsername());
                uaaFeignClient.syncUserAuth(dto);

                user.setAuthSynced(1);
                userMapper.updateById(user);
                success++;
            } catch (Exception e) {
                log.error("补偿任务：同步失败，userId={}, username={}，下次重试",
                        user.getId(), user.getUsername(), e);
            }
        }
        log.info("补偿任务完成：成功 {}, 失败 {}", success, unsynced.size() - success);
    }
}