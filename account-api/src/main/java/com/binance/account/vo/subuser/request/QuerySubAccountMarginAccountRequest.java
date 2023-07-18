package com.binance.account.vo.subuser.request;

import com.binance.account.vo.subuser.enums.MarginPeriodType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


@ApiModel("QuerySubAccountMarginAccountRequest")
@Data
public class QuerySubAccountMarginAccountRequest {


	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户邮箱")
    @NotBlank
    private String email;

    @ApiModelProperty(required = true, notes = "查询周期默认是TODAY")
    @NotNull
    private MarginPeriodType marginPeriodType;

}
