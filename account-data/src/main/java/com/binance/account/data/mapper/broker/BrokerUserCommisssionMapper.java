package com.binance.account.data.mapper.broker;

import com.binance.account.data.entity.broker.BrokerUserCommisssion;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface BrokerUserCommisssionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BrokerUserCommisssion record);

    int insertSelective(BrokerUserCommisssion record);

    BrokerUserCommisssion selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BrokerUserCommisssion record);

    int updateByPrimaryKey(BrokerUserCommisssion record);

    BrokerUserCommisssion selectByUserId(Long parentUserId);

    int updateByUserIdSelective(BrokerUserCommisssion record);

    List<BrokerUserCommisssion> selectAllNotInWhite(List<Long> list);

}