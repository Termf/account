package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("jumio SDK 初始化结果")
@Setter
@Getter
public class JumioSdkInitResponse implements Serializable {

    @ApiModelProperty("sdk init apiKey")
    private String apiKey;

    @ApiModelProperty("sdk init apiSecret")
    private String apiSecret;

    @ApiModelProperty("业务关联流水号")
    private String merchantReference;

    @ApiModelProperty("业务关联客户码")
    private String userReference;

    @ApiModelProperty("jumio info 的记录ID")
    private String jumioId;

    @ApiModelProperty("callback 地址")
    private String callBack;
}
