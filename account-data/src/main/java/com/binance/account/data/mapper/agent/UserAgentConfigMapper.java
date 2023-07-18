package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.UserAgentConfig;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserAgentConfigMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserAgentConfig record);

    int insertSelective(UserAgentConfig record);

    UserAgentConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserAgentConfig record);

    int updateByPrimaryKey(UserAgentConfig record);

    UserAgentConfig selectByUserId(Long id);

    List<UserAgentConfig> selectByPage(Map<String, Object> param);

    Integer countByUserId(Map<String, Object> param);

    int updateByUserId(UserAgentConfig param);
}