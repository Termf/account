package com.binance.account.mq;

import javax.annotation.Resource;

import com.binance.account.domain.bo.MsgNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OneButtonRegisterSender {
	@Resource
	private RabbitTemplate rabbitTemplate;

	/**
	 * 红包用户的一键注册，发送mq通知
	 * @param notification
	 */
	public void redPacketRegisterNotify(MsgNotification notification) {
		rabbitTemplate.convertAndSend("account.user.oneButtonRegister", "redbag.user.register", notification);
	}
}
