package com.binance.account.vo.withdraw.response;

import com.binance.account.common.enums.WithdrawFaceTipStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-12-20 15:01
 */
@ApiModel("提现是否需要做人脸识别提示内容")
@Setter
@Getter
public class UserWithdrawFaceTipResponse implements Serializable {

    private static final long serialVersionUID = 8089955727247534873L;

    @ApiModelProperty("提现是否需要做人脸识别的当前状态, " +
            "0-不需要做人脸识别 " +
            "1-需要先完成KYC认证 " +
            "2-可以进行人脸识别 " +
            "3-人脸识别待人工审核 " +
            "4-人脸识别已被拒绝,需联系客服")
    private int status;

    @ApiModelProperty("提示语")
    private String tip;

    @ApiModelProperty("做人脸识别的id")
    private String id;

    @ApiModelProperty("做人脸识别的type")
    private String type;
    
    @ApiModelProperty("SDK做人脸识别的二维码")
    private String qrCode;

    public UserWithdrawFaceTipResponse() {
        super();
        this.status = WithdrawFaceTipStatus.NORMAL.getCode();
        this.tip = null;
    }

    public UserWithdrawFaceTipResponse(int status, String tip) {
        super();
        this.status = status;
        this.tip = tip;
    }
}
