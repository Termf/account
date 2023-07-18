package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("EnableFastWithdrawSwitchResponse resp")
@Data
public class EnableFastWithdrawSwitchResponse {
    @ApiModelProperty("是否需要kyc")
    private Boolean needKyc=false;
}
