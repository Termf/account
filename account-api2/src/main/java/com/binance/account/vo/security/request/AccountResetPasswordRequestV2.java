package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

@ApiModel("AccountResetPasswordRequestV2")
@Getter
@Setter
public class AccountResetPasswordRequestV2  {


    @ApiModelProperty(name = "账号", required = true)
    private String email;


    @ApiModelProperty(name = "手机号", required = true)
    private String mobile;

    @ApiModelProperty(name = "手机国家码mobileCode", required = true)
    private String mobileCode;

    @ApiModelProperty(name = "新密码", required = true)
    @NotEmpty
    private String password;

    @ApiModelProperty(name = "确认密码", required = true)
    @NotEmpty
    private String passwordConfirm;


    @ApiModelProperty(required = true, notes = "新算法的密码")
    @NotEmpty
    private String safePassword;

    @ApiModelProperty(required = true, notes = "确认新算法的密码")
    @NotEmpty
    private String confirmSafePassword;

    @ApiModelProperty(name = "token", required = true)
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
