package com.binance.account.job;

import com.binance.account.service.kyc.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

public class KycCheckCanDoFaceTest extends BaseTest {

    @Resource
    private KycCheckCanDoFace kycCheckCanDoFace;

    @Test
    public void testExecute() throws Exception {
        kycCheckCanDoFace.execute("user:350607909");

        kycCheckCanDoFace.execute("user:350462089");
    }
}
