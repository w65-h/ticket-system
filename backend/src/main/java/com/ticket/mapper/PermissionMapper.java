package com.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticket.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.perm_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<Permission> selectPermissionsByUserId(Long userId);

    }