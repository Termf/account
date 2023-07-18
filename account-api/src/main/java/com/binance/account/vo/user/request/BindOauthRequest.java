package com.binance.account.vo.user.request;

import org.hibernate.validator.constraints.NotBlank;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("Oauth绑定")
@Getter
@Setter
public class BindOauthRequest extends ToString {
    private static final long serialVersionUID = 6940334057084641443L;
    @ApiModelProperty(required = true, notes = "第三方ID")
    @NotBlank
    private String clientId;
    @ApiModelProperty(required = true, notes = "用户邮箱")
    @NotBlank
    private String email;
    @ApiModelProperty(required = true, notes = "第三方返回的userId")
    @NotBlank
    private String oauthUserId;
    @ApiModelProperty(required = true, notes = "accessToken")
    @NotBlank
    private String accessToken;
    @ApiModelProperty(required = true, notes = "tokenType")
    private String tokenType;
    @ApiModelProperty(required = true, notes = "scope")
    private String scope;
    @ApiModelProperty(required = true, notes = "refreshToken")
    @NotBlank
    private String refreshToken;
    @ApiModelProperty(required = true, notes = "accessToken 过期时间")
    private int expiresIn;
    @ApiModelProperty(notes = "oauth绑定成功后跳转的地址")
    private String callback;



}
