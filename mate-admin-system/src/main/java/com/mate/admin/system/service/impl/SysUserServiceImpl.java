package com.mate.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.api.dto.UserRegisterDTO;
import com.mate.admin.api.feign.UaaFeignClient;
import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.mapper.SysUserMapper;
import com.mate.admin.system.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private UaaFeignClient uaaFeignClient;

    @Override
    public Map<String, Object> pageQuery(int pageNum, int pageSize,
                                          Long deptId, Integer status) {
        int offset = (pageNum - 1) * pageSize;
        List<SysUser> list = userMapper.selectUserPage(offset, pageSize, deptId, status);
        int total = userMapper.selectUserCount(deptId, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 创建用户：先落库（auto-commit），数据对 UAA 可见后再同步认证信息；
     * Feign 失败则补偿删除用户。
     */
    @Override
    public void addUser(SysUser user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        baseMapper.insert(user);

        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        try {
            uaaFeignClient.syncUserAuth(dto);
        } catch (Exception e) {
            log.error("UAA 认证同步失败，开始补偿删除用户。username={}, userId={}",
                    user.getUsername(), user.getId(), e);
            baseMapper.deleteById(user.getId());
            throw new RuntimeException("跨服务调用失败，用户创建已撤销，请重试", e);
        }
    }

    @Override
    public void updateUser(SysUser user) {
        user.setPassword(null);
        baseMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        baseMapper.deleteById(id);
    }
}
