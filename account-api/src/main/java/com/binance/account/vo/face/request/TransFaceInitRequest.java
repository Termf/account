package com.binance.account.vo.face.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-02-27 20:19
 */
@Setter
@Getter
public class TransFaceInitRequest extends ToString {

    @NotNull
    private String transId;

    @NotNull
    private Long userId;

    @ApiModelProperty("业务类型(对应人脸识别邮件中的type的值)")
    @NotNull
    private String type;

    @ApiModelProperty("是否需要发送邮件")
    private Boolean needEmail;

    @ApiModelProperty("是否为锁定一个用户一次KYC的认证流程")
    private boolean kycLockOne;
}
