package com.binance.account.vo.kyc.request;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("KYC人脸识别结果")
@Setter
@Getter
public class FaceAuthRequest extends KycFlowRequest {

    @ApiModelProperty("人脸识别状态")
    private TransFaceLogStatus status;

    @ApiModelProperty("人脸识别提示语信息")
    private String message;
    
    @ApiModelProperty("人脸审核状态")
    //admin 审核跳过人脸，赋值此字段。inspectore 人脸结果通知 赋值TransFaceLogStatus
    private KycCertificateStatus faceStatus;
    
    //新老迁移用到
    private boolean facePassed;


    // 操作人
    @ApiModelProperty("操作人")
    private String operator;

}
