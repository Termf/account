package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.MsgType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("发送绑定手机验证码Request")
@Getter
@Setter
public class SendBindMobileVerifyCodeRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 3603269138946361358L;

    @ApiModelProperty("用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("手机代码")
    private String mobileCode;

    @ApiModelProperty(required = false, notes = "pnk的verifyCode表主鍵")
    private String verifyCodeId;

    @ApiModelProperty("短信模板类型：文本或者语音")
    private MsgType msgType=MsgType.TEXT;


    @ApiModelProperty("是否是重新发送")
    private Boolean resend=false;

    @ApiModelProperty("是否为重置绑定手机，如果是，则不校验当前用户是否已经绑定过邮箱，否则会验证")
    private Boolean resetMobile = false;

}
