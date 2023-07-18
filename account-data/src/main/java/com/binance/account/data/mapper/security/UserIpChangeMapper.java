package com.binance.account.data.mapper.security;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.security.UserIpChange;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserIpChangeMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserIpChange record);

    int insertIgnore(UserIpChange record);

    int insertSelective(UserIpChange record);

    UserIpChange selectByPrimaryKey(@Param("id") String id, @Param("userId") Long userId);

    int updateByPrimaryKeySelective(UserIpChange record);

    int updateByPrimaryKey(UserIpChange record);

    UserIpChange selectByUserIdAndIp(@Param("userId")Long userId,@Param("ip")String ip);
}