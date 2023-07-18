package com.binance.account.data.entity.security;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSecurityCache extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -838526582335308833L;

    private Integer googleVerifyCode;

    private Long googleErrorTime;

    private Long smsErrorTime;

    private Long emailErrorTime;

    private Long webAuthnErrorTime;

    private Integer webAuthnErrorCount;

}
