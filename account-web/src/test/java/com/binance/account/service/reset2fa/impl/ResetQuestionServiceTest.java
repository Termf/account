package com.binance.account.service.reset2fa.impl;

import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.ProtectedStatus;
import com.binance.account.service.bigdata.AsyncBigDataProducer;
import com.binance.account.service.reset2fa.IResetQuestion;
import com.binance.account.vo.reset.request.ResetUserAnswerArg;
import com.binance.account.vo.reset.request.ResetUserReleaseArg;
import com.binance.account.vo.reset.request.UserResetBigDataLogRequestBody;
import com.binance.account.vo.reset.response.ResetUserAnswerBody;
import com.binance.account.vo.reset.response.ResetUserReleaseBody;
import com.binance.account.vo.reset.response.UserResetBigDataLogResponseBody;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class ResetQuestionServiceTest {
	@Resource
	IResetQuestion iResetQuestion;
	@Resource
	AsyncBigDataProducer producer;

	@Test
	public void testGetUserResetAnswers() {
		ResetUserAnswerArg body = new ResetUserAnswerArg();
		body.setUserId(350458720L);
		body.setResetId("a93a05910ab34587adbf32b7f37b2f1f");
		ResetUserAnswerBody res = iResetQuestion.getUserResetAnswers(body);
		assertTrue(res != null && res.getAnswers() != null);
		System.out.println(res);
	}

	@Test
	public void testReleaseFromProtectedMode() {
		ResetUserReleaseArg body = new ResetUserReleaseArg();
		body.setUserId(350458720L);
		body.setStatus(ProtectedStatus.FORBID_MODE);
		ResetUserReleaseBody res = iResetQuestion.releaseFromProtectedMode(body);
		assertTrue(res != null && res.isSuccess());
		body.setStatus(ProtectedStatus.PROTECTED_MODE);
		res = iResetQuestion.releaseFromProtectedMode(body);
		assertTrue(res != null && res.isSuccess());
		body.setStatus(ProtectedStatus.NORMAL_MODE);
		res = iResetQuestion.releaseFromProtectedMode(body);
		assertTrue(res != null && res.isSuccess());
	}
	
	@Test
	public void testGetUserResetBigDataLog() {
		String json ="{\n" + 
				"\"userId\": \"12345678\",\n" + 
				"\"type\": \"RESET_2FA_MODEL\",\n" + 
				"\"action\": \"ADD_USER_TAG\",\n" + 
				"\"description\": \"Reset2FA Risk - Daily Batch\",\n" + 
				"\"data\":\n" + 
				"\n" + 
				"{ \"userId\": \"36410292\", \"transId\": \"6082da1e4e944ab98de1f6951696f04e\", \"batchTime\": 1551697486530, \"score\": 0.7893 }\n" + 
				"}";
		Object parse = JSON.parse(json);
		int times=0;
		while (times-- > 0) {
			producer.produceMsgToBigData("bigdata-risk-notification", parse);
		}
		
		UserResetBigDataLogRequestBody body =new UserResetBigDataLogRequestBody();
		UserResetBigDataLogResponseBody res = iResetQuestion.getUserResetBigDataLog(body);
		System.out.println("res=>" + res);
	}

}
