package com.mate.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mate.admin.system.entity.SysDept;
import com.mate.admin.system.mapper.SysDeptMapper;
import com.mate.admin.system.service.SysDeptService;
import org.springframework.stereotype.Service;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {
}
