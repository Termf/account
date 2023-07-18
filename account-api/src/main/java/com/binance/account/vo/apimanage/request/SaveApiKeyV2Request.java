package com.binance.account.vo.apimanage.request;


import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel
@Getter
@Setter
public class SaveApiKeyV2Request extends BaseMultiCodeVerifyRequest {

    private static final long serialVersionUID = -4445310547066405042L;

    @ApiModelProperty
    @NotEmpty
    private String apiName;
    @ApiModelProperty
    private String info;
    @ApiModelProperty(required = true)
    @NotNull
    private String loginUserId;

    @ApiModelProperty
    private String publicKey;

    @ApiModelProperty(notes = "是否开启提币")
    private Boolean enableWithdrawStatus=false;
}
