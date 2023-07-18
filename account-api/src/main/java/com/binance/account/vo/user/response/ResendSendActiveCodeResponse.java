package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "重新发送激活Response", value = "重新发送激活Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ResendSendActiveCodeResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -131985148394570249L;

    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;

    @ApiModelProperty(readOnly = true, notes = "注册令牌")
    private String registerToken;

    @ApiModelProperty(readOnly = true, notes = "验证码")
    private String code;

}
