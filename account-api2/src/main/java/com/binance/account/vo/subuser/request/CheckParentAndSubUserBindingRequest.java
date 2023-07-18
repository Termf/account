package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhao chenkai
 */
@ApiModel("检查母子账号绑定关系，并返回子账号信息")
@Getter
@Setter
public class CheckParentAndSubUserBindingRequest {

    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户邮箱")
    @NotBlank
    private String email;

}
