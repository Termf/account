package com.binance.account.service.reset2fa.impl;

import com.binance.account.common.enums.ResetNextStep;
import com.binance.account.service.reset2fa.IReset2Fa;
import com.binance.account.vo.reset.request.ResetResendEmailRequest;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class Reset2FaServiceTest {

	@Resource
	IReset2Fa iReset2Fa;
	
	@Test
	public void testSendEmailAgain() {
		ResetResendEmailRequest body =new ResetResendEmailRequest();
		body.setUserId(350465595L);
		Reset2faNextStepResponse res = iReset2Fa.sendEmailAgain(body);
		assertTrue(res!=null&&res.getNextStep()==ResetNextStep.UPLOAD);
	}

}
