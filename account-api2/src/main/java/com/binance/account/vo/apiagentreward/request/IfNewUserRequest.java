package com.binance.account.vo.apiagentreward.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "查询用户是否满足返佣条件Request", value = "查询用户是否满足返佣条件Request")
@Data
public class IfNewUserRequest extends ToString {

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "api返佣码")
    @NotNull
    private String apiAgentCode;



}
