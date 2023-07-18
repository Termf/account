package com.binance.account.service.face.channel.risk;

import java.util.Map;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.service.face.channel.risk.handler.AbstractChannelRiskRatingHandler;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class UserChannelRiskRatingContext {
	
	private Map<UserRiskRatingChannelCode, AbstractChannelRiskRatingHandler> context;
	
	public UserChannelRiskRatingContext(Map<UserRiskRatingChannelCode, AbstractChannelRiskRatingHandler> context) {
		this.context = context;
	}
	
	public AbstractChannelRiskRatingHandler getRatingHandler(UserRiskRatingChannelCode channelCode) {
		AbstractChannelRiskRatingHandler handler = context.get(channelCode);
		if(handler == null) {
			log.error("获取渠道risk rating处理器失败. channelCode:{}",channelCode);
            throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		return handler;
	}
}
