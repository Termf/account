package com.binance.account.data.mapper.country;

import com.binance.account.data.entity.country.UserCountryWhitelist;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserCountryWhitelistMapper {

    boolean isInWhiteList(Long userId);

    int deleteByPrimaryKey(Long userId);

    int insertSelective(UserCountryWhitelist record);

    int insertOrUpdate(UserCountryWhitelist record);

    UserCountryWhitelist selectByPrimaryKey(Long userId);

    List<UserCountryWhitelist> selectWhiteList(Map<String, Object> params);

    int updateByPrimaryKeySelective(UserCountryWhitelist record);

}