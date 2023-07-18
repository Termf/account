package com.binance.account.service.question.export;

import java.util.List;
import java.util.Map;

import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.service.question.BO.OptionsWrapper;
import com.binance.account.service.question.BO.QuestionScoreBody;

/**
 * 风控提供的功能接口
 *
 */
public interface IDecisionMgmtService {

	/**
	 * 获取问题及其选项
	 * 
	 * @param userId
	 * @param riskTypes
	 * @return
	 */
	Map<String, OptionsWrapper> getQuestionOptions(Long userId, List<String> riskTypes);

	/**
	 * 推送用户答案，获取打分结果
	 * 
	 * @param userId
	 * @param scene 场景
	 * @param questionAnswers
	 * @return
	 */
	QuestionScoreBody getAnswerScore(Long userId, List<UserQuestionAnswers> questionAnswers);

	/**
	 * 是否需要回答问题
	 * 
	 * @param userId
	 * @param scene
	 * @return
	 */
	boolean needToAnswerQuestions(Long userId,QuestionSceneEnum scene, Map<String, String> device);

	/**
	 * 指定场景，查询用户命中的规则，决策系统api异常或失败则默认需要回答问题
	 * 
	 * @param userId
	 * @param scene
	 * @return 规则，为空不需要回答问题，与套题一一对应
	 */
	List<String> getRules(Long userId, QuestionSceneEnum scene);
	
}