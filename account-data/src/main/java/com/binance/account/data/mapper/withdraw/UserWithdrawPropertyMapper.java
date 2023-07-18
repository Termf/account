package com.binance.account.data.mapper.withdraw;

import com.binance.account.data.entity.withdraw.UserWithdrawProperty;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserWithdrawPropertyMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserWithdrawProperty record);

    int insertSelective(UserWithdrawProperty record);

    UserWithdrawProperty selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserWithdrawProperty record);

    int updateByPrimaryKey(UserWithdrawProperty record);
    
    int updateWithdrawLock(UserWithdrawProperty record);
}
