package com.mate.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mate.admin.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 用户分页查询（JOIN 部门表）
     * 说明：此查询走了 idx_dept_status 联合索引，
     * EXPLAIN type=ref, rows=千级
     */
    List<SysUser> selectUserPage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("deptId") Long deptId,
                                  @Param("status") Integer status);

    int selectUserCount(@Param("deptId") Long deptId,
                        @Param("status") Integer status);
}
