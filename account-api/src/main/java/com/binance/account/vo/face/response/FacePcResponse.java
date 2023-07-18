package com.binance.account.vo.face.response;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2019-03-01 13:08
 */
@ApiModel
@Setter
@Getter
public class FacePcResponse extends ToString {

    @ApiModelProperty("人脸识别是否通过")
    private boolean success;

    @ApiModelProperty("跳转路径")
    private String redirectPath;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("业务编号")
    private String transId;

    @ApiModelProperty("人脸识别流水编号")
    private String faceBizNo;

    @ApiModelProperty("人脸识别描述信息")
    private String faceRemark;

    @ApiModelProperty("业务人脸识别状态")
    private TransFaceLogStatus status;

    /**
     * 帮助结果判断的信息，是否为新版kyc认证的人脸识别
     */
    private boolean kycLockOne;
}
