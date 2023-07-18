package com.binance.account.data.mapper.security;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.security.UserQuestionOptions;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface UserQuestionOptionsMapper {

	/**
	 * 缓存表，物理删除
	 * 
	 * @param id
	 * @return
	 */
	int deleteByPrimaryKey(@Param("id") Long id);

	/**
	 * 保存选项
	 * 
	 * @param options
	 * @return
	 */
	int insert(UserQuestionOptions options);

	int insertBatch(@Param("options") List<UserQuestionOptions> options);

	/**
	 * 用户id与风控类型唯一定位选项
	 * 
	 * @param userId
	 * @param riskType
	 * @return
	 */
	UserQuestionOptions selectByPrimaryKey(@Param("userId") Long userId, @Param("riskType") String riskType);
}
