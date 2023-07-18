package com.binance.account.service.kyc;

import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.service.certificate.impl.UserCertificateBusiness;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.reset2fa.impl.Reset2FaService;
import com.binance.account.vo.reset.request.Reset2faNextStepRequest;
import com.binance.account.vo.reset.request.Reset2faStartValidatedRequest;
import com.binance.account.vo.reset.response.Reset2faStartValidatedResponse;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.HashMap;

public class Reset2FaSerTest extends BaseTest{
	
	@Resource
	Reset2FaService reset2FaService;
	
	@Mock
	IQuestion iQuestion;

	@Resource
	private UserCertificateBusiness userCertificateBusiness;

	@Test
	public void test1() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(reset2FaService, "iQuestion", iQuestion);
		
		
		Reset2faNextStepRequest request = new Reset2faNextStepRequest();
		request.setUserId(350462089l);
		request.setType(UserSecurityResetType.google);
		request.setDeviceInfo(new HashMap<String, String>());
		
		Mockito.when(iQuestion.needToAnswerQuestion(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
		
		reset2FaService.reset2faNextStepFlow(request);
	}
	
	@Test
	public void test2() {
		Reset2faStartValidatedRequest request = new Reset2faStartValidatedRequest(); 
		request.setUserId(350462089l);
		request.setType(UserSecurityResetType.google);
		request.setDeviceInfo(new HashMap<String, String>());
		
		Reset2faStartValidatedResponse resp = reset2FaService.reset2faStartValidated(request);
		System.out.println(resp);
	}

	@Test
	public void test3() {
		userCertificateBusiness.isIDNumberOccupied(null, null, null, 1231312L);
	}
}

