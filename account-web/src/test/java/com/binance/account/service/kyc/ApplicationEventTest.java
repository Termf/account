package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;

public class ApplicationEventTest extends BaseTest{
	@Resource
	ApplicationEventPublisher applicationEventPublisher;

	@Test
	public void test() {
		RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
		event.setTraceId("fdsafdsafdsa");
		event.setUserId(12343124321l);
		applicationEventPublisher.publishEvent(event);

	}
}
