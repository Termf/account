package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserTradingAccountMapper {
    int deleteByPrimaryKey(UserTradingAccount key);

    int insert(UserTradingAccount record);

    int insertSelective(UserTradingAccount record);

    int insertIgnore(UserTradingAccount record);

    Long queryUserIdByTradingAccount(@Param("tradingAccount") Long tradingAccount);

    Long queryTradingAccountByUserId(@Param("userId") Long userId);

}
