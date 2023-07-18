package com.binance.account.vo.face.response;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2018-12-13 10:58
 */
@ApiModel("SDK端人脸识别验证结构")
@Setter
@Getter
public class FaceSdkResponse extends ToString {

    @ApiModelProperty("验证通过/失败")
    private boolean success;
    @ApiModelProperty("失败的原因")
    private String message;
    @ApiModelProperty("提示头(成功或失败页面显示标题头)")
    private String title;
    @ApiModelProperty("提示内容(成功或失败页面显示内容)")
    private String content;
    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("业务编号")
    private String transId;
    @ApiModelProperty("人脸识别流水号")
    private String faceBizNo;
    @ApiModelProperty("业务状态")
    private TransFaceLogStatus status;

    /**
     * 帮助结果判断的信息，是否为新版kyc认证的人脸识别
     */
    private boolean kycLockOne;

}
