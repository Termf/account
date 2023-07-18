package com.binance.account.vo.certificate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("用户kyc认证表单输入地址")
@Data
public class KycFormAddrVo {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("国家")
    private String country;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("地址")
    private String addr;

}
