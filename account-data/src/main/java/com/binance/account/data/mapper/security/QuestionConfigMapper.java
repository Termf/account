package com.binance.account.data.mapper.security;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.security.QuestionConfig;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface QuestionConfigMapper {
	/**
	 * 表中所用问题配置
	 * 
	 * @return
	 */
	List<QuestionConfig> selectALL();
	
	/**
	 * 按照场景和套题查询,调教都可为空
	 * 
	 * @param scene
	 * @param set
	 * @return
	 */
	List<QuestionConfig> selectBy(@Param("scene") Integer sceneOrdnal,@Param("group") String group);

	/**
	 * 插入
	 * 
	 * @param question
	 * @return
	 */
	int insertOrUpdate(QuestionConfig questionConfig);

}
