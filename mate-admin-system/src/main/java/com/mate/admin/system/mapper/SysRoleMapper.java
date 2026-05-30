package com.mate.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mate.admin.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteRoleMenus(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_role_menu(role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    int insertRoleMenus(@Param("roleId") Long roleId,
                        @Param("menuIds") List<Long> menuIds);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("<script>" +
            "INSERT INTO sys_user_role(user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int insertUserRoles(@Param("userId") Long userId,
                        @Param("roleIds") List<Long> roleIds);
}
