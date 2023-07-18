package com.binance.account.vo.user.request;

import com.binance.account.vo.security.enums.MsgType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "发送短信认证码Request", value = "发送短信认证码Request")
@Getter
@Setter
public class SendSmsAuthCoderRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 3575814189700037341L;

    @ApiModelProperty(required = false, notes = "userId(2选一)")
    private Long userId;

    @ApiModelProperty(required = false, notes = "邮箱（2选一）")
    private String email;

    @ApiModelProperty(required = false, notes = "pnk的verifyCode表主鍵")
    private String verifyCodeId;

    @ApiModelProperty("短信模板类型：文本或者语音")
    private MsgType msgType=MsgType.TEXT;

    @ApiModelProperty("是否是重新发送")
    private Boolean resend=false;

}
