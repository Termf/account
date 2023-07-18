package com.binance.account.data.mapper.broker;

import com.binance.account.data.entity.broker.ApiAgentUserAlias;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface ApiAgentUserAliasMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ApiAgentUserAlias record);

    int insertSelective(ApiAgentUserAlias record);

    ApiAgentUserAlias selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ApiAgentUserAlias record);

    int updateByPrimaryKey(ApiAgentUserAlias record);

    ApiAgentUserAlias selectByAgentIdCustomerId(ApiAgentUserAlias searchParam);

    ApiAgentUserAlias selectByAgentIdRefereeId(ApiAgentUserAlias searchParam);

    List<ApiAgentUserAlias> selectByAgentIdCustomerIdEmail(ApiAgentUserAlias searchParam);

    List<ApiAgentUserAlias> selectByEmails(Map<String, Object> searchMap);
}