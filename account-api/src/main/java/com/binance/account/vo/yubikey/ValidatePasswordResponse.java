package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class ValidatePasswordResponse implements Serializable {

    @ApiModelProperty("密码正确返回的流水号")
    private String serialNo;

}
