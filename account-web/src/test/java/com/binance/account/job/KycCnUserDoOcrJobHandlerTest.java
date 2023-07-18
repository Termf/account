package com.binance.account.job;

import com.binance.account.Application;
import com.binance.account.service.kyc.BaseTest;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = Application.class)
public class KycCnUserDoOcrJobHandlerTest extends BaseTest {


    @Resource
    private KycCnUserDoOcrJobHandler jobHandler;

    @Test
    public void testJob() throws Exception{
        String param = "flag:1";
        jobHandler.execute(param);
    }

}
