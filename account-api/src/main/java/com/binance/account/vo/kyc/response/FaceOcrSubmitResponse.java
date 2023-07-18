package com.binance.account.vo.kyc.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FaceOcrSubmitResponse extends KycFlowResponse {

    @ApiModelProperty("ocr 状态")
    private String status;

    @ApiModelProperty("提示语信息")
    private String message;
}
