package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("开启提币白名单Response")
@Getter
@Setter
@NoArgsConstructor
public class OpenWithdrawWhiteStatusV2Response extends ToString {
    private static final long serialVersionUID = -2610480800525509109L;

}
