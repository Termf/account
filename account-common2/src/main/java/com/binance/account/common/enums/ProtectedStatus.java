package com.binance.account.common.enums;

/**
 * 用后2fa重置答题失败后状态
 * 
 * @author zwh-binance
 *
 */
public enum ProtectedStatus {
	/**
	 * 正常模式
	 */
	NORMAL_MODE,
	/**
	 * 保护模式,重置答题受限
	 */
	PROTECTED_MODE,
	/**
	 * 禁用模式,受限模式待定
	 */
	FORBID_MODE;

	/**
	 * 当前状态是否处于受限状态（非NORMAL状态）
	 * 
	 * @return
	 */
	public boolean isInProtectedMode() {
		return this != NORMAL_MODE;
	}
}
