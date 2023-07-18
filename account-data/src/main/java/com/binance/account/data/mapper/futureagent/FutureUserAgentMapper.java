package com.binance.account.data.mapper.futureagent;

import com.binance.account.data.entity.futureagent.FutureUserAgent;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface FutureUserAgentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FutureUserAgent record);

    int insertSelective(FutureUserAgent record);

    FutureUserAgent selectByPrimaryKey(Long id);

    FutureUserAgent selectByAgentCode(String agentCode);

    int updateByPrimaryKeySelective(FutureUserAgent record);

    int updateByPrimaryKey(FutureUserAgent record);

    FutureUserAgent selectByUserId(Long userId);

    FutureUserAgent selectByFutureUserId(Long userId);

    List<FutureUserAgent> selectByFutureUserIds(List<Long> futureUserIds);

}