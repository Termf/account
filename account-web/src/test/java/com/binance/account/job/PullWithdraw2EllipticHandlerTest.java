package com.binance.account.job;

import com.binance.account.service.kyc.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PullWithdraw2EllipticHandlerTest extends BaseTest {
    @Autowired
    private PullWithdraw2EllipticHandler pullWithdraw2EllipticHandler;

    @Test
    public void test() {
        pullWithdraw2EllipticHandler.execute("{\"batchSize\":\"10\",\"range\":\"300\",\"full\":\"true\"}");
    }
}
