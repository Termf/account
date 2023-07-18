package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@ApiModel("解绑手机Request")
@Getter
@Setter
public class UnbindMobileRequest implements Serializable {

    private static final long serialVersionUID = 5324613982842595608L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("密码")
    @NotEmpty
    private String password;

    @ApiModelProperty("短信验证码")
    @NotEmpty
    private String smsCode;

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
