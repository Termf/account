package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("SelectApiAgentCodeAliasRes")
@Data
public class SelectApiAgentCodeAliasRes {

    @ApiModelProperty(required = false, notes = "三方备注id")
    private String customerId;

    @ApiModelProperty(required = false, notes = "email")
    private String email;

    @ApiModelProperty(required = false, notes = "被推荐人id")
    private Long refereeId;

}