package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by pcx
 */
@Data
public class CreateNoEmailSubUserResp  {

    @ApiModelProperty("母账号UserId")
    private Long parentUserId;

    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;
/*
    @ApiModelProperty(readOnly = true, notes = "密码加密")
    private String salt;

    @ApiModelProperty(readOnly = true, notes = "密码加密后的")
    private String password;*/

    @ApiModelProperty(readOnly = false, notes = "推荐人")
    private Long agentId;

   /* @ApiModelProperty(readOnly = true, notes = "注册令牌")
    private String registerToken;

    @ApiModelProperty(readOnly = true, notes = "验证码")
    private String code;

    @ApiModelProperty(readOnly = true, notes = "设备指纹id")
    private String currentDeviceId;*/
}