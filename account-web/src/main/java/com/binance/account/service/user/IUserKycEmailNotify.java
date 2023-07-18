package com.binance.account.service.user;

import com.binance.account.common.enums.UserKycEmailNotifyType;

public interface IUserKycEmailNotify {
	/**
	 * 添加用户Basic邮件通知任务
	 */
	void addBasicNotifyTask(Long userId,String email);
	
	/**
	 * 添加用户TRADE邮件通知任务
	 */
	void addTradeNotifyTask(Long userId,String email);
	
	/**
	 * 添加充值邮件通知任务
	 * @param userId
	 * @param email
	 */
	boolean needDepositNotifyTask(Long userId);
	
	/**
	 * 添加充值邮件通知任务
	 * @param userId
	 * @param email
	 */
	void addDepositNotifyTask(Long userId,String email);
	
	/**
	 * 交易满额1000/10000USD邮件通知
	 * @param userId
	 * @param email
	 * @param type
	 */
	void addTradeCompleteTask(Long userId, String email,UserKycEmailNotifyType type);
	
	/**
	 * 重置用户邮件通知任务
	 */
	void reset(Long userId);
	
	/**
	 * 执行任务
	 */
	void doTask();
	
}
