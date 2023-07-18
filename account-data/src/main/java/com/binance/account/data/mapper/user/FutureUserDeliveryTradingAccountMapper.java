package com.binance.account.data.mapper.user;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.user.FutureUserDeliveryTradingAccount;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface FutureUserDeliveryTradingAccountMapper {
    int deleteByPrimaryKey(Long deliveryTradingAccount);

    int insert(FutureUserDeliveryTradingAccount record);

    int insertSelective(FutureUserDeliveryTradingAccount record);

    FutureUserDeliveryTradingAccount selectByPrimaryKey(Long deliveryTradingAccount);

    int updateByPrimaryKeySelective(FutureUserDeliveryTradingAccount record);

    int updateByPrimaryKey(FutureUserDeliveryTradingAccount record);

    Long queryUserIdByDeliveryTradingAccount(@Param("deliveryTradingAccount") Long tradingAccount);
}