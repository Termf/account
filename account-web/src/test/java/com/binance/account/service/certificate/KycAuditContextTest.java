package com.binance.account.service.certificate;

import com.binance.account.Application;
import com.binance.account.service.certificate.executor.TestEnv;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class KycAuditContextTest extends TestEnv {

    @Resource
    private UserKycBusiness userKycBusiness;

    @Test
    public void testAutoAudit() throws Exception {
        userKycBusiness.syncUserKycCanAutoPass(380967555989565441l, 350462089l);
        Thread.sleep(360000);
    }

}
