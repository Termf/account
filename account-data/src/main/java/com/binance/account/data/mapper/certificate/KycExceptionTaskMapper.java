package com.binance.account.data.mapper.certificate;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.certificate.KycExceptionTask;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface KycExceptionTaskMapper {

	List<KycExceptionTask> selectPage(@Param("startTime") Date startTime, @Param("endTime") Date endTime,
			@Param("status") String status, @Param("start") int start, @Param("rows") int rows);

	KycExceptionTask selectByUk(@Param("userId") Long userId, @Param("taskType") String taskType);
	
	int updateByPrimaryKeySelective(KycExceptionTask record);
	
	int deleteByUk(@Param("userId") Long userId, @Param("taskType") String taskType);

	int insert(KycExceptionTask record);

	int insertSelective(KycExceptionTask record);
}