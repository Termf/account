package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel("重置密码验证码验证Response")
@Getter
@Setter
@ToString
public class AccountResetPasswordVerifyResponse implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7889272857430126216L;
    private Long userId;

    public AccountResetPasswordVerifyResponse(Long userId) {
        super();
        this.userId = userId;
    }

    public AccountResetPasswordVerifyResponse() {
        super();
    }
    
    
}
