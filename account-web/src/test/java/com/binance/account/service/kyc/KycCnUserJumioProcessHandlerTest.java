package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.job.KycCnUserJumioProcessHandler;

public class KycCnUserJumioProcessHandlerTest extends BaseTest{

	@Resource
	KycCnUserJumioProcessHandler KycCnUserJumioProcessHandler;
	
	@Test
	public void test() {
		try {
			KycCnUserJumioProcessHandler.execute("flag:10");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
