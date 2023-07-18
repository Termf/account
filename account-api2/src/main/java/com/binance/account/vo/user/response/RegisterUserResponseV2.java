package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "RegisterUserResponseV2", value = "注册Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class RegisterUserResponseV2 extends ToString {


    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;

    @ApiModelProperty(readOnly = true, notes = "密码加密")
    private String salt;

    @ApiModelProperty(readOnly = true, notes = "密码加密后的")
    private String password;

    @ApiModelProperty(readOnly = false, notes = "推荐人")
    private Long agentId;

    @ApiModelProperty(readOnly = true, notes = "注册令牌")
    private String registerToken;

    @ApiModelProperty(readOnly = true, notes = "验证码")
    private String code;

    @ApiModelProperty(readOnly = true, notes = "设备指纹id")
    private String currentDeviceId;

}
