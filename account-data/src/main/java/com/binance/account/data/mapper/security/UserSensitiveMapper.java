package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserSensitive;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserSensitiveMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserSensitive record);

    int insertSelective(UserSensitive record);

    UserSensitive selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserSensitive record);

    int updateByPrimaryKey(UserSensitive record);
}