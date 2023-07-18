package com.binance.account.vo.apimanage.request;


import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel
@Getter
@Setter
public class SaveApiKeyRequest extends ToString {

    private static final long serialVersionUID = -4445310547066405042L;
    @ApiModelProperty(value = "后端创建APIkey的目标用户ID")
    private String targetUserId;
    @ApiModelProperty
    private String ruleId;
    @ApiModelProperty
    @NotEmpty
    private String apiName;
    @ApiModelProperty
    private String info;
    @ApiModelProperty
    private AuthTypeEnum operationType;
    @ApiModelProperty
    private String verifyCode;
    @ApiModelProperty
    private String ip;
    @ApiModelProperty(required = true)
    @NotNull
    private String loginUserId;
    @ApiModelProperty(required = true)
    @NotNull
    private Boolean backend;
    @ApiModelProperty("邮件中的链接，若传了则用外部的，否则用默认的")
    private String emailLink;

    @ApiModelProperty
    private String publicKey;

    @ApiModelProperty(notes = "是否开启提币")
    private Boolean enableWithdrawStatus=false;
}
