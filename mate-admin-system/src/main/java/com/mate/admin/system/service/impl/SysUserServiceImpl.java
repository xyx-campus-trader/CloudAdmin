package com.mate.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysUser;
import com.mate.admin.system.mapper.SysUserMapper;
import com.mate.admin.system.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private SysUserMapper userMapper;

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

    @Override
    public void addUser(SysUser user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        baseMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        user.setPassword(null); // 不更新密码
        baseMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        baseMapper.deleteById(id);
    }
}
