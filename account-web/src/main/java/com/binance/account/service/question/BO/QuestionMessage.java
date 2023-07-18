package com.binance.account.service.question.BO;

import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

/**
 * 问题模块，答题完毕后发的消息
 *
 */
@Getter
@Setter
public class QuestionMessage extends ToString {
	private static final long serialVersionUID = -5451183276952651488L;
	
	/**
	 * user id
	 */
	private long userId;
	/**
	 * flow id
	 */
	private String flowId;
	/**
	 * flow Type
	 */
	private String flowType;
	/**
	 * result of which user answer the questions
	 */
	private AnswerResponseBody result;
	
}
