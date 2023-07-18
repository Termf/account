package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("SendEmailVerifyCodeResponse")
@Data
public class SendEmailVerifyCodeResponse extends ToString {
    private static final long serialVersionUID = -7501014648174005753L;
}

