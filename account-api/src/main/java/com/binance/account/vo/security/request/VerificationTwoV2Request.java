package com.binance.account.vo.security.request;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("验证手机验证码/谷歌验证码Request")
@Getter
@Setter
public class VerificationTwoV2Request extends VarificationTwoRequest {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("scenario")
    //@NotNull
    private SecurityKeyApplicationScenario scenario;
    
}
