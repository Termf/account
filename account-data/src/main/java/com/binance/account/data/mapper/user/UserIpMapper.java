package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserIp;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface UserIpMapper {
    int deleteByPrimaryKey(UserIp key);

    int insert(UserIp record);

    int insertSelective(UserIp record);

    Long queryByCount(@Param("userId") Long userId, @Param("ip") String ip);

    int insertIgnore(UserIp userIp);

    int getIpCount(@Param("userId") Long userId, @Param("ip") String ip);

    List<UserIp> getIpByUser(Long userId);
}
