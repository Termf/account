package com.binance.account.job;

import com.binance.account.Application;
import com.binance.account.service.kyc.BaseTest;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author mikiya.chen
 * @date 2020/2/9 3:46 下午
 */

@SpringBootTest(classes = Application.class)
public class KycCertificateInfoSupplyJobHandlerTest extends BaseTest {

    @Resource
    private KycCertificateInfoSupplyJobHandler jobHandler;

    @Test
    public void testJob() throws Exception{
        String param = "{\"userId\": 35033101}";
        jobHandler.execute(param);
    }
}
