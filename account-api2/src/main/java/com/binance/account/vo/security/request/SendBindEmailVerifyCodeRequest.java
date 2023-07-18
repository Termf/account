package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("SendBindEmailVerifyCodeRequest")
@Getter
@Setter
public class SendBindEmailVerifyCodeRequest extends ToString {
    private static final long serialVersionUID = 5630177550845020444L;

    @ApiModelProperty("用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty("邮箱")
    @NotNull
    private String email;

    @ApiModelProperty("是否是重新发送")
    private Boolean resend=false;

    @ApiModelProperty("是否为重置绑定邮箱，如果是，则不校验当前用户是否已经绑定过邮箱，否则会验证")
    private Boolean resetEmail = false;

}
