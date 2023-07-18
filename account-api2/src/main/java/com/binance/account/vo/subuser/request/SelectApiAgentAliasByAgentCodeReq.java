package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("SelectApiAgentAliasByAgentCodeReq")
@Data
public class SelectApiAgentAliasByAgentCodeReq {


	@ApiModelProperty(required = true, notes = "apiAgentCode")
    @NotNull
    private String apiAgentCode;

    @ApiModelProperty(required = false, notes = "refereeId")
    private Long refereeId;

}