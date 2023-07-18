package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("GetUserEmailAndMobileByUserIdRequest")
@Data
public class GetUserEmailAndMobileByUserIdRequest {
    @ApiModelProperty("用户userid")
    @NotNull
    private Long userId;

}
