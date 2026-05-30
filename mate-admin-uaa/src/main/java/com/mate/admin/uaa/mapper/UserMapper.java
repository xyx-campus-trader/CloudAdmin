package com.mate.admin.uaa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mate.admin.uaa.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
