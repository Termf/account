package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@ApiModel("QuerySubAccountMarginAccountSummaryRequest")
@Data
public class QuerySubAccountMarginAccountSummaryRequest {


	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "子账户开启状态,1:开启; 0:未开启")
    private Integer isSubUserEnabled;

    private Integer page;

    private Integer rows;

}
