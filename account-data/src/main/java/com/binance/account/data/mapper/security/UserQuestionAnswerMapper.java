package com.binance.account.data.mapper.security;

import com.binance.account.common.query.QuestionQuery;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 2fa重置回答问题答案记录表
 */
@DefaultDB
public interface UserQuestionAnswerMapper {

	UserQuestionAnswers selectByPrimaryKey(@Param("id") Long id, @Param("userId") Long userId);

	/**
	 * 按照索查询答案
	 * 
	 * @param userId 必填
	 * @param flowId 必填
	 * @param answerId 不必填
	 * @param questionId 不必填
	 * @return
	 */
	List<UserQuestionAnswers> selectByKey(@Param("userId") Long userId, @Param("flowId") String flowId,
										  @Param("answerId") Long answerId, @Param("questionId") Long questionId);

	List<UserQuestionAnswers> getListByUser(QuestionQuery questionQuery);

	int insert(UserQuestionAnswers answer);
	
	int deleteByPrimaryKey(@Param("id") Long id,  @Param("userId") Long userId);
	
	int updateSelective(UserQuestionAnswers answer);

	/**
	 * 查询流程当前到答题次数
	 * @param userId
	 * @param flowId
	 * @return
	 */
    Integer getFlowCurrentAnswerTimes(@Param("userId") Long userId, @Param("flowId") String flowId);
}
