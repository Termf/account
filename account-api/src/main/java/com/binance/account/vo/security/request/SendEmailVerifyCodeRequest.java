package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@ApiModel("SendEmailVerifyCodeRequest")
@Getter
@Setter
public class SendEmailVerifyCodeRequest extends ToString {


    private static final long serialVersionUID = -5805203904385125590L;
    @ApiModelProperty("用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty("是否是重新发送")
    private Boolean resend=false;

    @ApiModelProperty("业务场景")
    @NotNull
    private BizSceneEnum bizScene;

    /**
     * 各场景所需邮件参数Key
     * api_key_manage:apiName
     */
    @ApiModelProperty("邮件所需参数")
    private Map<String, Object> params = new HashMap<>();
}
