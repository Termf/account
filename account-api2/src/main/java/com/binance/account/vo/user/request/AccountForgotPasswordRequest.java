package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

@ApiModel("账号忘记密码Request")
@Getter
@Setter
public class AccountForgotPasswordRequest implements Serializable {

    private static final long serialVersionUID = 2720295187453293481L;

    @ApiModelProperty(name = "登录名", required = false)
    private String email;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customEmailLink;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
