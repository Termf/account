package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.mq.KycAddressOcrSender;
import com.binance.inspector.vo.ocr.request.OcrDetectTextRequest;

public class KycAddressOcrSendTest extends BaseTest{
	@Resource
	KycAddressOcrSender kycAddressOcrSender;
	@Test
	public void test() {
//		350465820
		
		OcrDetectTextRequest body = new OcrDetectTextRequest();
		body.setUserId(350465820L);
		body.setImage("/ADDRESS_IMG_20190813/350513820_7761489.png");
		body.setFirstName("base.getFirstName()");
		body.setMiddleName("base.getMiddleName()");
		body.setLastName("base.getLastName()");
		kycAddressOcrSender.notifyMq(body);
		
	}

}
