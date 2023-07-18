package com.binance.account.data.mapper.useroperation;

import com.binance.account.data.entity.log.UserPreferLog;
import com.binance.master.annotations.DefaultDB;
import org.javasimon.aop.Monitored;


@DefaultDB
@Monitored
public interface UserPreferLogMapper {

    int insert(UserPreferLog record);
}