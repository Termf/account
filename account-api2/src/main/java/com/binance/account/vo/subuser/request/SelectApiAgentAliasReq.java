package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("SelectApiAgentAliasReq")
@Data
public class SelectApiAgentAliasReq {


	@ApiModelProperty(required = true, notes = "agentId")
    @NotNull
    private Long agentId;

    @ApiModelProperty(required = false, notes = "三方备注id")
    private String customerId;

    @ApiModelProperty(required = false, notes = "email")
    private String email;

    private Integer page;

    private Integer size;
}