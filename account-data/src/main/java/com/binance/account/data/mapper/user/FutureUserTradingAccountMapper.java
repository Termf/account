package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.FutureUserTradingAccount;
import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface FutureUserTradingAccountMapper {
    int deleteByPrimaryKey(FutureUserTradingAccount key);

    int insert(FutureUserTradingAccount record);

    int insertSelective(FutureUserTradingAccount record);

    int insertIgnore(FutureUserTradingAccount record);

    Long queryUserIdByTradingAccount(@Param("tradingAccount") Long tradingAccount);

    Long queryTradingAccountByUserId(@Param("userId") Long userId);

}
