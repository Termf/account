package com.binance.account.vo.user.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.enums.MsgType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "发送短信认证码Request", value = "发送短信认证码Request")
@Getter
@Setter
public class SendSmsAuthCodeV2Request extends ToString {
    private static final long serialVersionUID = -233361134549989760L;

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

    @ApiModelProperty("业务场景")
    @NotNull
    private BizSceneEnum bizScene;

    /**
     * 各场景所需参数Key
     * api_key_manage:apiName
     */
    @ApiModelProperty("邮件所需参数")
    private Map<String, Object> params = new HashMap<>();
}
