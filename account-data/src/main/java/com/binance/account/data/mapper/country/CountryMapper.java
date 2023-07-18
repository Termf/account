package com.binance.account.data.mapper.country;

import com.binance.account.data.entity.country.Country;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface CountryMapper {
    int deleteByPrimaryKey(String code);

    int insert(Country record);

    int insertSelective(Country record);

    Country selectByPrimaryKey(String code);

    Country selectByMobileCode(String code);


    int updateByPrimaryKeySelective(Country record);

    int updateByPrimaryKey(Country record);

    Country selectByCode2(String code2);

    List<Country> selectCountryList();

    List<Country> selectAllCountryList();

    Country selectByNationality(String nationality);
}
