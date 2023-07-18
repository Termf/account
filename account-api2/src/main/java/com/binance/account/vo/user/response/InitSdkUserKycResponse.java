package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("SDK端初始化User-KYC结果")
@Data
public class InitSdkUserKycResponse implements Serializable {

    private static final long serialVersionUID = 1397746120892660264L;

    @ApiModelProperty("初始化JUMIO SDK的API KEY")
    private String apiKey;

    @ApiModelProperty("初始化JUMIO SDK的API SECRET")
    private String apiSecret;

    @ApiModelProperty("业务流水号")
    private String merchantReference;

    @ApiModelProperty("用户流水号")
    private String userReference;

    @ApiModelProperty("callback 地址")
    private String callBack;
}
