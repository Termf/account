package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel(description = "重新发送激活Request", value = "重新发送激活Request")
@Getter
@Setter
public class ResendSendActiveCodeRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1851994786176964021L;

    @ApiModelProperty(required = true, notes = "邮箱")
    private String email;

    // 格式须满足例如：http://binance.com/resetPassword.html?vc={vc}&email={email}
    @ApiModelProperty(name = "自定义邮件链接-用于独立服务(Info等)", required = false)
    private String customEmailLink;

    @ApiModelProperty(required = false, notes = "是否走新的登录流程")
    private Boolean isNewLoginProcess=false;

}
