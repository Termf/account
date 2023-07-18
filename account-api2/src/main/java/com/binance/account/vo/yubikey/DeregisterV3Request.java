package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@ApiModel("解绑Webauthn")
@Data
public class DeregisterV3Request extends DeregisterV2Request {

    @NotBlank
    private String credentialId;

}
