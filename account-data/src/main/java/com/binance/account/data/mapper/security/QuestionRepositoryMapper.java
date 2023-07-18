package com.binance.account.data.mapper.security;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface QuestionRepositoryMapper {
	/**
	 * 表中所用问题配置
	 * 
	 * @return
	 */
	List<QuestionRepository> selectALL();
	
	/**
	 * 表中所有启用的问题
	 * 
	 * @return
	 */
	List<QuestionRepository> selectEnableALL();
	
	
	
	/**
	 * 查询指定套题的全部题目
	 * 
	 * @param set
	 * @param riskType
	 * @return
	 */
	List<QuestionRepository> selectBy(@Param("group") String group, @Param("riskType") String riskType);

	/**
	 * 逻辑删除,禁用
	 * 
	 * @param id
	 * @return
	 */
	int deleteByPrimaryKey(@Param("id") Long id);
	
	/**
	 * 启用
	 * 
	 * @param id
	 * @return
	 */
	int enable(@Param("id") Long id);

	/**
	 * 插入
	 * 
	 * @param question
	 * @return
	 */
	int insertOrUpdate(QuestionRepository question);

}
