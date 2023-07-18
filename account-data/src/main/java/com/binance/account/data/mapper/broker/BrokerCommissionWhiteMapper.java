package com.binance.account.data.mapper.broker;

import com.binance.account.data.entity.broker.BrokerCommissionWhite;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface BrokerCommissionWhiteMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BrokerCommissionWhite record);

    int insertSelective(BrokerCommissionWhite record);

    BrokerCommissionWhite selectByPrimaryKey(Long id);

    BrokerCommissionWhite selectByUserId(Long userId);

    List<Long> selectAll();

    int updateByPrimaryKeySelective(BrokerCommissionWhite record);

    int updateByPrimaryKey(BrokerCommissionWhite record);
}