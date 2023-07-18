package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;

import com.binance.account.job.UserKycDataMigrationJobHandler;
import com.binance.account.service.certificate.IUserKycDataMigration;

public class UserKycDataMigrationJobHandlerTest extends BaseTest{
	
	@Resource
	UserKycDataMigrationJobHandler userKycDataMigrationJobHandler;
	
	@Resource
	IUserKycDataMigration iUserKycDataMigration;
	
	@Test
	public void test() {
		try {
			userKycDataMigrationJobHandler.execute(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test2() {
		try {
			iUserKycDataMigration.moveToKycCertificateByUserId(350608260L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
