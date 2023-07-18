package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

@ApiModel("激活openId response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class OpenIdActiveResponse extends ToString {

    private static final long serialVersionUID = -5119407013803130639L;
    @ApiModelProperty(notes = "accessToken")
    private String accessToken;
    @ApiModelProperty(notes = "refreshToken")
    private String refreshToken;
    @ApiModelProperty(notes = "跳转地址")
    private String callback;
    @ApiModelProperty(notes = "tokenType")
    private String tokenType;
    @ApiModelProperty(notes = "scope")
    private String scope;
    @ApiModelProperty(notes = "accessToken 过期时间")
    private int expiresIn;
}
