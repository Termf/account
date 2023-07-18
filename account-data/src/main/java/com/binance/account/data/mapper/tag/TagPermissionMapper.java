package com.binance.account.data.mapper.tag;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.tag.TagPermission;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface TagPermissionMapper {

    Integer insertBatch(@Param("permissions") List<TagPermission> permissions);

    Integer deleteByRoleId(@Param("roleId") String roleId);

    List<TagPermission> selectByRoleId(@Param("roleId") String roleId);

    List<TagPermission> selectByRoleIds(@Param("roleIds") List<String> roleIds);

}
