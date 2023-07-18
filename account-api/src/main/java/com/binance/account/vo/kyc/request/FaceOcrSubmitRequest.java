package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("Kyc face ocr request body")
@Data
public class FaceOcrSubmitRequest extends KycFlowRequest {

    @ApiModelProperty("提交类型: FACE/OCR")
    private String type;


    private String face;

    private String front;

    private String back;
}
