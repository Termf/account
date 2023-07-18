package com.binance.account.service.question.options;

import com.binance.account.service.question.options.impl.CurrencySelectedGenerator;
import com.binance.account.service.question.options.impl.RandomInternalGenerator;
import com.binance.account.service.question.options.impl.RegistrationDataGenerator;

/**
 * 问题选项的类型，每个类型有相同的选项生成逻辑
 *
 */
public enum QustionOptionsTypeEnum {

	/**
	 * 币种选择，更具用户持有的币种+收藏的币种+混淆币种生成
	 */
	CURRENCY_SELECTED(CurrencySelectedGenerator.class),
	/**
	 * 持币数量随机区间,用户持币数量随机区间
	 */
	RANDOM_NUMBER_INTERNAL(RandomInternalGenerator.class),
	/**
	 * 用户注册时间
	 */
	USER_REGISTRATION_DATE(RegistrationDataGenerator.class);
	
	private Class<? extends IOptionsService> beanType;
	
	QustionOptionsTypeEnum(Class<? extends IOptionsService> beanType) {
		this.beanType = beanType;
	}

	public Class<? extends IOptionsService> getBeanType() {
		return beanType;
	}
}
