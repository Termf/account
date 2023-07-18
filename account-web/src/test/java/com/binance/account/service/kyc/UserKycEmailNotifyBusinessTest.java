package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.service.user.IUserKycEmailNotify;
public class UserKycEmailNotifyBusinessTest extends BaseTest{
	
	@Resource
     IUserKycEmailNotify iUserKycEmailNotify;
	
	@Test
	public void addBasicNotifyTask() {
		iUserKycEmailNotify.addBasicNotifyTask(350462089l, "leon.liu@binance.com");
	}
	@Test
	public void addTradeNotifyTask() {
		iUserKycEmailNotify.addTradeNotifyTask(350462089l, "leon.liu@binance.com");
	}
	@Test
	public void reset() {
		iUserKycEmailNotify.doTask();
		iUserKycEmailNotify.reset(350462089l);
	}
}
