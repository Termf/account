package com.binance.account.data.mapper.certificate;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.certificate.CountryState;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface CountryStateMapper {
	int deleteByPrimaryKey(CountryState key);

	int insert(CountryState record);

	int insertSelective(CountryState record);

	CountryState selectByPrimaryKey(CountryState key);

	int updateByPrimaryKeySelective(CountryState record);

	int updateByPrimaryKey(CountryState record);
	
	List<CountryState> selectAll();
}