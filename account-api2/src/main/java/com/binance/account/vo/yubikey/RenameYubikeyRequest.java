package com.binance.account.vo.yubikey;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RenameYubikeyRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String origin;

    @NotBlank
    private String credentialId;

    @NotBlank
    @Max(30)
    @Min(1)
    private String nickName;

}
