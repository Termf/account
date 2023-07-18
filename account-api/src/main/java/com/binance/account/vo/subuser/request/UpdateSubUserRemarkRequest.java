package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by pcx
 */
@ApiModel("UpdateSubUserRemarkRequest")
@Getter
@Setter
public class UpdateSubUserRemarkRequest {


	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户email")
    @NotBlank
    private String subUserEmail;

    @ApiModelProperty(required = true, notes = "备注")
    private String remark;


}
