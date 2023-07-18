package com.binance.account.mq;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.CacheRefreshType;
import com.binance.account.vo.kyc.CacheRefreshVo;

@Service
public class CacheRefreshMsgSender {

	@Resource
	private RabbitTemplate rabbitTemplate;

	
	public void notifyMQ(CacheRefreshType type) {
		CacheRefreshVo vo = new CacheRefreshVo();
		vo.setType(type);
		rabbitTemplate.convertAndSend("account.cache.refresh", null, vo);
	}
}
