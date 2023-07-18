package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.UserAgentRate;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserAgentRateMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserAgentRate record);

    long insertSelective(UserAgentRate record);

    UserAgentRate selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserAgentRate record);

    int updateByPrimaryKey(UserAgentRate record);

    List<UserAgentRate> selectByUserIdAgentCode(Map<String,Object> param);

    Integer countByUserIdAgentCode(Map<String,Object> param);

    Integer countByUserId(Long userId);

    UserAgentRate selectByAgentCode(String agentId);

    int updateAgentLabel(Map<String, Object> params);

    UserAgentRate selectCheckedShareCodeByUserId(Long userId);

    int deleteAllShareCodeByUserId(Map<String, Object> searchParam);

    int updateOneAsAgentCode(Map<String, Object> searchParam);
}