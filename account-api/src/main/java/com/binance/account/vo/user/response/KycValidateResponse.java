package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KycValidateResponse extends ToString {

    private static final long serialVersionUID = 6473498312315361723L;

    @ApiModelProperty(name = "是否通过，1是，0否")
    private String isValidated;

    @ApiModelProperty(name = "所属组")
    private String group;

    @ApiModelProperty(name = "kyc或者地址认证标识")
    private String kycOrAddress;

    @ApiModelProperty(name = "地址认证的具体地址")
    private String fullAddress;

    @ApiModelProperty(name = "拒绝原因")
    private String failReason;
}
