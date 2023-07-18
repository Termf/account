package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("开启提币白名单Request")
@Getter
@Setter
public class OpenWithdrawWhiteStatusV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = 6078977857529458996L;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;
}
