package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("AccountForgotPasswordPreCheckRequest")
@Getter
@Setter
public class AccountForgotPasswordPreCheckRequest extends BaseMultiCodeVerifyRequest{


    @ApiModelProperty(name = "登录名", required = false)
    private String email;

    @ApiModelProperty(name = "手机号", required = true)
    private String mobile;

    @ApiModelProperty(name = "手机国家码mobileCode", required = true)
    private String mobileCode;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
