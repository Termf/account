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

    private String flowDefine;

    @ApiModelProperty("用户上传文件接口返回的文件唯一号, 如果当前接口中传入了则会忽略face的二进制文件数据")
    private String faceFileKey;

    @ApiModelProperty("用户上传文件接口返回的文件唯一号, 如果当前接口中传入了则会忽略front的二进制文件数据")
    private String frontFileKey;

    @ApiModelProperty("用户上传文件接口返回的文件唯一号, 如果当前接口中传入了则会忽略back的二进制文件数据")
    private String backFileKey;
}
