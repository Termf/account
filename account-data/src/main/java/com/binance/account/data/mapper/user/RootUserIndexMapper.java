package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.RootUserIndex;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface RootUserIndexMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(RootUserIndex record);

    int insertSelective(RootUserIndex record);

    RootUserIndex selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(RootUserIndex record);

    int updateByPrimaryKey(RootUserIndex record);

    List<RootUserIndex> selectByUserIds(List<Long> userIds);
}