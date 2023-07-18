package com.binance.account.data.mapper.oauth;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.oauth.OauthBind;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface OauthBindMapper {
    int deleteById(Integer id);

    int insert(OauthBind record);

    int bind(Integer id);

    OauthBind selectByClientAndOauthUserId(@Param("clientId") String clientId, @Param("oauthUserId") String oauthUserId);
}
