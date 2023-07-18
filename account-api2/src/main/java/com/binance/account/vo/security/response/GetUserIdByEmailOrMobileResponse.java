package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("GetUserIdByEmailOrMobileResponse")
@Data
public class GetUserIdByEmailOrMobileResponse {
    @ApiModelProperty("用户userid")
    private Long userId;
}
