package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "一键注册用户账号激活Request", value = "一键注册用户账号激活Request")
@Getter
@Setter
public class OneButtonUserAccountActiveRequest extends BaseMultiCodeVerifyRequest {

    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("手机代码")
    private String mobileCode;
}
