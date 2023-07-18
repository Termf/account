package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("CreateApiAgentAliasReq")
@Data
public class CreateApiAgentAliasReq {


	@ApiModelProperty(required = true, notes = "推荐用户id")
    @NotNull
    private Long agentId;

    @ApiModelProperty(required = false, notes = "三方备注id")
    @NotBlank
    private String customerId;

    @ApiModelProperty(required = false, notes = "email")
    @NotBlank
    private String email;
}