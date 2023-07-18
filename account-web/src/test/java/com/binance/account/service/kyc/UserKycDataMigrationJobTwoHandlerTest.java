package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.job.UserKycDataMigrationJobHandler;
import com.binance.account.job.UserKycDataMigrationJobTwoHandler;
import com.binance.account.service.certificate.IUserKycDataMigration;

public class UserKycDataMigrationJobTwoHandlerTest extends BaseTest{
	
	@Resource
	UserKycDataMigrationJobTwoHandler userKycDataMigrationJobTwoHandler;
	
	@Resource
	IUserKycDataMigration iUserKycDataMigration;
	
	@Test
	public void test() {
		try {
			userKycDataMigrationJobTwoHandler.execute(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
