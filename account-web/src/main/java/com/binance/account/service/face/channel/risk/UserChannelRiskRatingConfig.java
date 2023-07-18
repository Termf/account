package com.binance.account.service.face.channel.risk;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.service.face.channel.risk.handler.AbstractChannelRiskRatingHandler;
import com.google.common.collect.Maps;

@Configuration
public class UserChannelRiskRatingConfig {

	@Resource
	private ApplicationContext applicationContext;

	@Bean
	public UserChannelRiskRatingContext getUserChannelRiskRatingContext() {
		Map<UserRiskRatingChannelCode, AbstractChannelRiskRatingHandler> context = Maps.newHashMap();
		Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(UserChannelRiskRatingHandler.class);
		for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
			AbstractChannelRiskRatingHandler ratingHandler = (AbstractChannelRiskRatingHandler) entry.getValue();
			UserChannelRiskRatingHandler handler = ratingHandler.getClass()
					.getAnnotation(UserChannelRiskRatingHandler.class);
			UserRiskRatingChannelCode[] types = handler.handlerType();
			for (UserRiskRatingChannelCode userRiskRatingChannelCode : types) {
				if (handler != null && handler.handlerType() != null) {
					context.put(userRiskRatingChannelCode, ratingHandler);
				}
			}
		}
		return new UserChannelRiskRatingContext(context);
	}
}
