package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class BindingParentSubUserEmailResp {

    @ApiModelProperty(required = true, notes = "子账号UserId")
    private Long subUserId;

}