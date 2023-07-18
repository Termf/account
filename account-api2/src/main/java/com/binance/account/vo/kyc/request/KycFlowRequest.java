package com.binance.account.vo.kyc.request;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.master.enums.TerminalEnum;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("Kyc流程请求")
@Setter
@Getter
public class KycFlowRequest implements Serializable {


    private static final long serialVersionUID = 6516906408321992734L;
    
    private Long userId;
    
    private KycCertificateKycType kycType;
    
    private TerminalEnum source;
    
    private boolean lockJumio = false;
    
}
