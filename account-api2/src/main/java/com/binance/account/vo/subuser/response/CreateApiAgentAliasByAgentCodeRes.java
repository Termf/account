package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("CreateApiAgentAliasByAgentCodeReq")
@Data
public class CreateApiAgentAliasByAgentCodeRes {


	@ApiModelProperty(required = true, notes = "apiAgentCode")
    private String apiAgentCode;

    @ApiModelProperty(required = false, notes = "三方备注id")
    private String customerId;

}