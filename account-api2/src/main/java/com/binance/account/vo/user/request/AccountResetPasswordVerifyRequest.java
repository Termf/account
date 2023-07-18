package com.binance.account.vo.user.request;

import java.io.Serializable;

import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("重置密码验证码验证Request")
@Getter
@Setter
@ToString
public class AccountResetPasswordVerifyRequest implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 7625796530112552048L;

    @ApiModelProperty(name = "账号", required = true)
    @NotEmpty
    private String email;
    
    @ApiModelProperty(name = "令牌", required = true)
    @NotEmpty
    private String token;
}
