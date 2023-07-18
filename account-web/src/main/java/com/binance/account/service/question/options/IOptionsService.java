package com.binance.account.service.question.options;

import java.util.List;

public interface IOptionsService {

	final String NONE_OF_ABOVE = "NONE_OF_THE_ABOVE";

	/**
	 * 为制定类型的问题生成问题选项
	 * 
	 * @param userId
	 * @param qustionType
	 * @return
	 */
	List<String> genaerateOptions(Long userId);

	/**
	 * 获取问题对应的正确答案，应该是选项的子集
	 * 
	 * @param userId
	 * @param questionName 问题名称
	 * @return
	 */
	List<String> getCorrectAnswers(Long userId, final UserQuestionEnum questionName);
	
	/**
	 * 修整正确答案集合，过滤掉选项中没有的项，类似correctAnswers.retainAll(options);
	 * 这个方法在保存问题/选项/答案的时候调用
	 * 
	 * @param options 有缓存的问题选项，不一定是最新的
	 * @param correctAnswers 正确答案
	 * @return 调整后的正确答案
	 */
	List<String> trimming(List<String> options, List<String> correctAnswers);
}
