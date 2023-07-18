package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("解绑手机Request")
@Getter
@Setter
public class UnbindMobileV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -1282337434788582125L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

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
