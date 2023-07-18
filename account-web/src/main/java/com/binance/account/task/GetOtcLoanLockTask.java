package com.binance.account.task;

import com.binance.account.integration.featureservice.FeatureValueApiApiClient;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
@Log4j2
public class GetOtcLoanLockTask implements Callable<BigDecimal> {

    private FeatureValueApiApiClient featureValueApiApiClient;
    private Long userId;

    public GetOtcLoanLockTask(FeatureValueApiApiClient featureValueApiApiClient, Long userId) {
        this.featureValueApiApiClient = featureValueApiApiClient;
        this.userId = userId;
    }

    @Override
    public BigDecimal call() throws Exception {
        log.info("GetOtcLoanLockTask start:userId={}",userId);
        BigDecimal result=featureValueApiApiClient.getOtcLoanLockBtc(userId);
        log.info("GetOtcLoanLockTask end:userId={}",userId);
        return result;
    }
}
