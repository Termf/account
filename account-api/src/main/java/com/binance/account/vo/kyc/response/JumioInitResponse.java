package com.binance.account.vo.kyc.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("web jumio result")
@Setter
@Getter
public class JumioInitResponse extends KycFlowResponse {

    private static final long serialVersionUID = -5882738256957133276L;

    // web 端使用
    @ApiModelProperty("web jumio page url")
    private String redirectUrl;

    // sdk 端使用
    @ApiModelProperty("初始化JUMIO SDK的API KEY")
    private String apiKey;

    @ApiModelProperty("初始化JUMIO SDK的API SECRET")
    private String apiSecret;

    @ApiModelProperty("业务流水号")
    private String merchantReference;

    @ApiModelProperty("用户流水号")
    private String userReference;

    @ApiModelProperty("sdk callback 地址")
    private String callBack;

    @ApiModelProperty("业务编号(对应人脸识别邮件中的id的值)")
    private String transId;

    @ApiModelProperty("业务类型(对应人脸识别邮件中的type的值)")
    private String type;

}
