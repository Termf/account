package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("CreateApiAgentAliasByAgentCodeReq")
@Data
public class CreateApiAgentAliasByAgentCodeReq {


	@ApiModelProperty(required = true, notes = "apiAgentCode")
    @NotBlank
    private String apiAgentCode;

    @ApiModelProperty(required = false, notes = "三方备注id")
    @NotBlank
    private String customerId;

    @ApiModelProperty(required = false, notes = "reffeeId")
    @NotNull
    private Long reffeeId;
}