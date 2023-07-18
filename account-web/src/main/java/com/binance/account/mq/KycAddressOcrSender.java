package com.binance.account.mq;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.binance.inspector.vo.ocr.request.OcrDetectTextRequest;

@Service
public class KycAddressOcrSender {
	@Resource
	private RabbitTemplate rabbitTemplate;
	
	public void notifyMq(OcrDetectTextRequest request) {
		rabbitTemplate.convertAndSend("account.kyc.common", "kyc.address.ocr", request);
	}
}
