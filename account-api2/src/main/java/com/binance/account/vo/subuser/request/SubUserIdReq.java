package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/10.
 */
@Data
public class SubUserIdReq {

    @ApiModelProperty(required = true, notes = "子账号UserId")
    @NotNull
    private Long subUserId;

    @ApiModelProperty(required = false, notes = "子账号备注")
    private String remark;
}