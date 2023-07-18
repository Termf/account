package com.binance.account.vo.kyc.response;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("KYC 流程请求结果")
@Getter
@Setter
public class KycFlowResponse extends ToString {

    private static final long serialVersionUID = 8111204707240664274L;

    private Long userId;

    private KycCertificateKycType kycType;
    
    private String trip;

}
