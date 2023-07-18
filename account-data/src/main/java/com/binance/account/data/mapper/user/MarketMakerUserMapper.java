package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.MarketMakerUser;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface MarketMakerUserMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(MarketMakerUser record);

    int insertSelective(MarketMakerUser record);

    MarketMakerUser selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(MarketMakerUser record);

    int updateByPrimaryKey(MarketMakerUser record);

    List<MarketMakerUser> selectDynamic(MarketMakerUser queryDO);


}
