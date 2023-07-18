package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


@ApiModel("SubAccountMarginEnableRequest")
@Data
public class SubAccountMarginEnableRequest {


	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户邮箱")
    @NotBlank
    private String email;

    @ApiModelProperty(required = true, notes = "开通关闭账户")
//    @NotNull
    private Boolean enable = true;

}
