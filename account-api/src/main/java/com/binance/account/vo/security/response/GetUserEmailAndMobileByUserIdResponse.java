package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("GetUserEmailAndMobileByUserIdResponse")
@Data
public class GetUserEmailAndMobileByUserIdResponse {

    @ApiModelProperty("邮箱")
    private String email;


    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("手机代码")
    private String mobileCode;

}
