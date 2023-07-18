package com.binance.account.mq;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.vo.kyc.CacheRefreshVo;
import com.binance.master.utils.TrackingUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import com.rabbitmq.client.Channel;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CacheRefreshMsgListener {

	@RabbitListener(
			bindings = @QueueBinding(value = @Queue(durable = "false", autoDelete = "true"),
			exchange = @Exchange(value = "account.cache.refresh", type = ExchangeTypes.FANOUT, durable = "false")),
			containerFactory = "rabbitListenerContainerFactory")
	public void onMessage(Message message, @Header(AmqpHeaders.CHANNEL) Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) {
		TrackingUtils.putTracking("MQ_CACHE_REFRESH", UUIDUtils.getId());
		String body = null;
		try {
			body = new String(message.getBody(), "UTF-8");
			log.info("刷新Local缓存收到MQ消息.");
			if (StringUtils.isBlank(body)) {
				log.warn("刷新Local缓存收到MQ消息错误.");
				return;
			}
			CacheRefreshVo req = JSON.parseObject(body, CacheRefreshVo.class);
			if (req == null || req.getType() == null) {
				log.warn("刷新Local缓存消息解析后数据缺失");
				return;
			}

            switch (req.getType()) {
			case COUNTRY_STATE:
				CountryStateHelper.init();
				break;
			case MESSAGE_MAP:
				MessageMapHelper.initCache();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error("刷新Local缓存处理异常. body:{}", body, e);
		} finally {
			try {
				channel.basicAck(deliveryTag, false);
			} catch (Exception e) {
				log.error("刷新Local缓存处理 消息ACK失败. ", e);
			}
			TrackingUtils.removeTracking();
		}
	}
}
