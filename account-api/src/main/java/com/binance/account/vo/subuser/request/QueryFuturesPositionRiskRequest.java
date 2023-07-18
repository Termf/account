package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@ApiModel("QueryFuturesPositionRiskRequest")
@Data
public class QueryFuturesPositionRiskRequest {

    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户邮箱")
    @NotBlank
    private String email;
}
