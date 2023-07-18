package com.binance.account.service.reset2fa.strategy;

/**
 * 策略触发器，明知指定的规则时，触发后续操作
 * 
 * @author zwh-binance
 *
 */
public interface IstrategyTrigger {

	/**
	 * 是否触发指定的规则
	 * 
	 * @return
	 */
	public boolean isTriggerRules();

	/**
	 * 触发规则后执行次操作
	 */
	public void postProcess();
}
