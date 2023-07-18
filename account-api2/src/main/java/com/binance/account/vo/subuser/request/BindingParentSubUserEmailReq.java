package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


@Data
public class BindingParentSubUserEmailReq {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账号的email")
    @NotBlank
    private String subUserEmail;

}