package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@ApiModel("SubAccountMarginEnableResp")
@Data
public class SubAccountMarginEnableResp {

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty("是否启用margin")
    private Boolean isMarginEnabled;

}
