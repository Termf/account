package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("GetSubBindingInfoByPageReq")
@Data
public class GetSubBindingInfoByPageReq {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "经销商子账户id")
    private Long subUserId;

    @ApiModelProperty(required = false, notes = "经销商子账户邮箱")
    private String subUserEmail;


    @ApiModelProperty(required = false, notes = "页码，从1开始")
    private Integer page;

    @ApiModelProperty(required = false, notes = "每一页数量")
    private Integer size;
}
