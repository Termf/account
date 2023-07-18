package com.binance.account.service.question.export;

import com.binance.account.service.question.BO.QuestionMessage;

/**
 * 问答模块同步回调接口
 * 
 * @author zwh-binance
 *
 */
public interface IQuestionHnadler {

	/**
	 * 处理回答问题的最终结果
	 * 
	 * @param msg
	 */
	void invoke(QuestionMessage msg);
}
