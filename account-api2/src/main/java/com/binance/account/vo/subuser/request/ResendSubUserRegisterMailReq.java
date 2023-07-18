package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResendSubUserRegisterMailReq extends ToString {

    private static final long serialVersionUID = 7900997028686805016L;

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账号邮箱")
    @NotNull
    private String subUserEmail;

    @ApiModelProperty(name = "自定义邮件链接", required = false)
    private String customEmailLink;

}
