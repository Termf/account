package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("开启或关闭提币白名单Request")
@Getter
@Setter
public class OpenOrCloseWithdrawWhiteStatusRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 7920159337729909151L;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull()
    private Long userId;

    @ApiModelProperty(required = false, notes = "认证类型")
    private AuthTypeEnum authType;

    @ApiModelProperty(required = false, notes = "2次验证码")
    private String code;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customForbiddenLink;
}
