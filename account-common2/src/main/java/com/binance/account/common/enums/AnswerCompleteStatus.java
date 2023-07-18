package com.binance.account.common.enums;

/**
 * 答题状态枚举
 * 
 * @author zwh-binance
 *
 */
public enum AnswerCompleteStatus {
	/**
	 * 当前答题完成,请继续
	 */
	OK,
	/**
	 * 回答完毕,但是不正确
	 */
	Fail,
	/**
	 * 答题超时
	 */
	TimeOut,
	/**
	 * 答题成功
	 */
	Success;
}