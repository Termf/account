package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("发送绑定手机验证码Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SendBindMobileVerifyCodeResponse extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 7520019886369036810L;

    private String code;

}

