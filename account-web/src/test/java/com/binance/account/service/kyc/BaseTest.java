package com.binance.account.service.kyc;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.binance.account.Application;




@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class BaseTest {
	static { 
		System.setProperty("eureka.instance.metadataMap.envflag", "lf");
        System.setProperty("env", "dev");
        System.setProperty("gray", "dev");
        System.setProperty("com.binance.secretsManager.accessKey", "AKIASQS2UI6PJPEGV5MG");
        System.setProperty("com.binance.secretsManager.secretKey", "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");
        System.setProperty("db.sharding.table.init.switch","false");
	}
}
