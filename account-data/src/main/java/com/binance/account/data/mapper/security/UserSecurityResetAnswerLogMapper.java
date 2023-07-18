package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserSecurityResetAnswerLog;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface UserSecurityResetAnswerLogMapper {

    int insert(UserSecurityResetAnswerLog record);

    UserSecurityResetAnswerLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserSecurityResetAnswerLog record);

    List<UserSecurityResetAnswerLog> getByResetId(String resetId);

}