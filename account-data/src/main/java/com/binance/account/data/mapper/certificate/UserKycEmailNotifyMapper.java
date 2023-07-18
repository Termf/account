package com.binance.account.data.mapper.certificate;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.certificate.UserKycEmailNotify;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserKycEmailNotifyMapper {
	int insert(UserKycEmailNotify record);

	int insertSelective(UserKycEmailNotify record);

	UserKycEmailNotify selectByPrimaryKey(@Param("userId") Long userId, @Param("type") String type);

	int updateByPrimaryKeySelective(UserKycEmailNotify record);

	int deleteByUserId(Long userId);

	List<UserKycEmailNotify> selectPage(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
			@Param("status") String status,@Param("start") int start,@Param("rows") int rows);
}