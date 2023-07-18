package com.binance.account.service.reset2fa.strategy;

import java.util.List;

import lombok.Builder;

/**
 * 流程粗略执行器,当命中定义的规则后执行指定的
 * 
 * @author zwh-binance
 *
 */
@Builder
public class StrategyExecutor {

	private final List<IstrategyTrigger> triggers;
	
	public void execute() {
		triggers.forEach(trigger -> {
			if (trigger.isTriggerRules()) {
				trigger.postProcess();
			}
		});
	}
}
