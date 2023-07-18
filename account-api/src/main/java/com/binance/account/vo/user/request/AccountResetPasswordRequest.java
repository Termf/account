package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import com.binance.master.validator.constraints.FieldMatch;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Map;

@ApiModel("重置密码Request")
@FieldMatch(first = "password", second = "passwordConfirm")
@Getter
@Setter
public class AccountResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = -462040574600644430L;

    @ApiModelProperty(name = "账号", required = true)
    @NotEmpty
    private String email;

    @ApiModelProperty(name = "新密码", required = true)
    @NotEmpty
    private String password;

    @ApiModelProperty(name = "确认密码", required = true)
    @NotEmpty
    private String passwordConfirm;

    @ApiModelProperty(name = "令牌", required = true)
    @NotEmpty
    private String token;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customForbiddenLink;

    @ApiModelProperty(readOnly = true, notes = "设备信息")
    private Map<String, String> deviceInfo;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
