package com.binance.account.data.mapper.broker;

import com.binance.account.data.entity.broker.BrokerCommissionUpdateBak;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface BrokerCommissionUpdateBakMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BrokerCommissionUpdateBak record);

    int insertSelective(BrokerCommissionUpdateBak record);

    BrokerCommissionUpdateBak selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BrokerCommissionUpdateBak record);

    int updateByPrimaryKey(BrokerCommissionUpdateBak record);

    Long countAllBak();

    List<BrokerCommissionUpdateBak> selectByPage(Map<String,Object> map);

}