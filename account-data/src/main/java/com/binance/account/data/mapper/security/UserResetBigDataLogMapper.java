package com.binance.account.data.mapper.security;

import java.util.List;

import com.binance.account.data.entity.security.UserResetBigDataLog;
import com.binance.account.data.entity.security.UserResetBigDataLogQuery;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserResetBigDataLogMapper {

	List<UserResetBigDataLog> select(UserResetBigDataLogQuery query);
	
	int insert(UserResetBigDataLog data);
}
