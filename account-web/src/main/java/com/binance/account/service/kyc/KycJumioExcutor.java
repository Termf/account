package com.binance.account.service.kyc;

import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@Service("KycJumioExcutor")
public class KycJumioExcutor extends AbstractKycFlowCommonExecutor{

    @Override
    public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
        return null;
    }
}
