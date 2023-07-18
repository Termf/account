package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserAgentLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserAgentLog record);

    int insertSelective(UserAgentLog record);

    UserAgentLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserAgentLog record);

    int updateByPrimaryKey(UserAgentLog record);

    Integer countByAgentCode(String agentCode);

    List<UserAgentLog> selectByAgentCode(Map<String, Object> userParam);

    UserAgentLog selectByReferralUserId(Long userId);

    long countUserType0();

    List<UserAgentLog> selectUserType0(Map<String,Object> param);

    int updateUserType(UserAgentLog userAgentLog);

    Integer countByAgentId(Long userId);

    List<UserAgentLog> selectByUserIdAgentCode(Map<String, Object> userParam);

    Integer countByAgentCodes(Map<String, Object> searchParam);

    long selectAgentNumByRegisterTime(Map<String, Object> searchParam);

    Integer countByUserIdAgentCode(Map<String, Object> param);

    long countUserTypeNormal();

    List<UserAgentLog> selectUserTypeNormal(Map<String, Object> param);

}