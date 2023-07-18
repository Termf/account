package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.MiningAgentLog;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface MiningAgentLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(MiningAgentLog record);

    int insertSelective(MiningAgentLog record);

    MiningAgentLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MiningAgentLog record);

    int updateByPrimaryKey(MiningAgentLog record);

    Long countByUserIdAgentCode(Map<String, Object> param);

    List<MiningAgentLog> selectByUserIdAgentCode(Map<String, Object> userParam);
}