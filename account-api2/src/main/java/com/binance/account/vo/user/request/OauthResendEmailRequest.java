package com.binance.account.vo.user.request;

import org.hibernate.validator.constraints.NotBlank;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "oauth重发激活邮件Request")
@Getter
@Setter
public class OauthResendEmailRequest extends ToString {
    private static final long serialVersionUID = 5649274833563604228L;
    @ApiModelProperty(required = true, notes = "邮箱")
    @NotBlank
    private String email;

    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase().trim();
    }
}
