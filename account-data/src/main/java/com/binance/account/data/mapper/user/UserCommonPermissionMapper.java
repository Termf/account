package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserCommonPermission;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface UserCommonPermissionMapper {
    int deleteByPrimaryKey(String userType);

    int insert(UserCommonPermission record);

    int insertSelective(UserCommonPermission record);

    UserCommonPermission selectByPrimaryKey(String userType);

    List<UserCommonPermission> selectAll();


    int updateByPrimaryKeySelective(UserCommonPermission record);

    int updateByPrimaryKey(UserCommonPermission record);
}