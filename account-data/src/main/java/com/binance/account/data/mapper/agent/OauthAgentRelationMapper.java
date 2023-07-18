package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.OauthAgentRelation;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface OauthAgentRelationMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OauthAgentRelation record);

    int insertSelective(OauthAgentRelation record);

    OauthAgentRelation selectByPrimaryKey(Long id);

    OauthAgentRelation selectByOauthCode(String oauthCode);

    int updateByPrimaryKeySelective(OauthAgentRelation record);

    int updateByPrimaryKey(OauthAgentRelation record);
}