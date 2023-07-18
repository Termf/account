package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.SecurityFaceStatusSource;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-11-29 10:44
 */
@Setter
@Getter
public class SecurityFaceStatusRequest implements Serializable {
    private static final long serialVersionUID = -1323133961531450984L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("提现风控是否需要做人脸识别 0:不需要;1:需要")
    @NotNull
    private Integer withdrawSecurityFaceStatus;

    @ApiModelProperty("提现业务标识(如果是创建提币人脸识别记录时存在则记录下来)")
    private String withdrawId;

    @ApiModelProperty("提现风控是否需要做人脸识别 请求来源")
    private SecurityFaceStatusSource source;

    @ApiModelProperty("是否需要发送邮件, 默认需要")
    private boolean needEmail = true;
}
