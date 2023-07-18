package com.binance.account.service.question.options;

import com.binance.account.service.question.Utils;


/**
 * 问题选项生成服务
 *
 */
public class OptionsGeneratorFactory  {

	public static IOptionsService getOptionsService(final UserQuestionEnum questionType) {
		Class<? extends IOptionsService> clz = questionType.getOptionType().getBeanType();
		return Utils.getBean(clz);
	}
}
