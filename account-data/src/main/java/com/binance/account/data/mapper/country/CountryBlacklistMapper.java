package com.binance.account.data.mapper.country;

import com.binance.account.data.entity.country.CountryBlacklist;
import com.binance.master.annotations.DefaultDB;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface CountryBlacklistMapper {
    int deleteByPrimaryKey(String countryCode);

    int insert(CountryBlacklist record);

    int insertSelective(CountryBlacklist record);

    CountryBlacklist selectByPrimaryKey(String countryCode);

    List<Map> selectAll();

    int updateByPrimaryKeySelective(CountryBlacklist record);

    int updateByPrimaryKey(CountryBlacklist record);
}