package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("VerifyFundPasswordResponse")
@Data
public class VerifyFundPasswordResponse {

    private boolean verifyResult=false;

    private Integer availableRetryTimes;

}
