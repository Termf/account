package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import com.binance.master.validator.groups.Auth;
import com.binance.master.validator.groups.Select;
import com.binance.master.validator.regexp.Regexp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@ApiModel(description = "LoginUserRequestV2", value = "登录Request")
@Getter
@Setter
public class LoginUserRequestV2 extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -8303100150934746780L;

    @ApiModelProperty(required = true, notes = "账号")
    @NotEmpty(groups = {Select.class, Auth.class})
    @Email(groups = {Select.class, Auth.class}, regexp = Regexp.LOGIN_EMAIL)
    private String email;

    @ApiModelProperty(required = false, notes = "密码")
    @NotEmpty(groups = Select.class)
    private String password;

    @ApiModelProperty(required = true, notes = "是否2次验证")
    @NotNull(groups = {Select.class, Auth.class})
    private Boolean isAuth;

    @ApiModelProperty(required = false, notes = "是否走大户保护逻辑")
    private Boolean isBigProtection;

    @ApiModelProperty(required = false, notes = "设备信息")
    private HashMap<String, String> deviceInfo;

    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customDeviceAuthorizeUrl;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customForbiddenLink;

    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customIpChangeConfirmLink;

    @ApiModelProperty(name = "设备授权成功后的跳转地址", required = false)
    private String callback;

    @ApiModelProperty(required = false, notes = "是否走新的登录流程")
    private Boolean isNewLoginProcess=false;


    @ApiModelProperty(required = false, notes = "是否是手机号注册的版本")
    private Boolean isMobileUserVersion =false;

    public void setEmail(String email) {
        this.email = StringUtils.trimToEmpty(email).toLowerCase();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }


}
