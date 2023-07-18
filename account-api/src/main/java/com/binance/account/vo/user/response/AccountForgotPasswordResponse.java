package com.binance.account.vo.user.response;

import java.io.Serializable;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("账号忘记密码Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountForgotPasswordResponse implements Serializable {

    private static final long serialVersionUID = -2512842860812292108L;

    @ApiModelProperty(required = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(required = true, notes = "账号")
    private String email;

    @ApiModelProperty(required = true, notes = "状态")
    private Long status;

    @ApiModelProperty(required = true, notes = "token")
    private String token;

    @ApiModelProperty(readOnly = true, notes = "禁用token")
    private String disableToken;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
