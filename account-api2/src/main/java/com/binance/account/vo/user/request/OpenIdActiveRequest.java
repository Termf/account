package com.binance.account.vo.user.request;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Edit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "openId激活Request")
@Getter
@Setter
public class OpenIdActiveRequest extends ToString {
    private static final long serialVersionUID = 1461818595098475540L;
    @ApiModelProperty(required = true, notes = "邮箱")
    @NotBlank
    private String email;

    @ApiModelProperty(required = true, notes = "验证码")
    @NotBlank
    private String verifyCode;

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase().trim();
    }
}
