package com.binance.account.data.mapper.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.common.enums.UserRiskStatus;
import com.binance.account.data.entity.user.UserRiskFeature;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserRiskFeatureMapper {

	/**
	 * 保存数据
	 * 
	 * @param featur
	 * @return
	 */
	int insert(UserRiskFeature featur);

	/**
	 * 按照更新时间,查询x分钟之前的status状态的数据
	 * 
	 * @param x
	 * @param status {@link UserRiskStatus#ordinal()}
	 * @return
	 */
	List<UserRiskFeature> getBeforeMinutes(@Param("minute") Integer x, @Param("status") Integer status,
			@Param("sessionId") Integer sessionId);

	
	/**
	 * 按照用户和流程查询记录
	 * 
	 * @param userId
	 * @param flowId
	 * @return
	 */
	List<UserRiskFeature> getByUserId(@Param("userId") Long userId, @Param("flowId") String flowId);
	
	/**
	 * 按照更新时间,更新x分钟之前的数据状态，从‘from’到‘to’
	 * 
	 * @param x
	 * @param from {@link UserRiskStatus#ordinal()}
	 * @param to   {@link UserRiskStatus#ordinal()}
	 * @return
	 */
	int updateStatusFromTo(@Param("minute") Integer x, @Param("from") Integer from, @Param("to") Integer to,
			@Param("sessionId") Integer sessionId);
	
	/**
	 * 更新doing状态的数据
	 * 
	 * @param feature
	 * @return
	 */
	int updateSelectiveInDoing(UserRiskFeature feature);
	
	/**
	 * 删除指定id的数据
	 * 
	 * @param id
	 * @return
	 */
	int delete(@Param("id") Long id);
}
