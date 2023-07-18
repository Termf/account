package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/19.
 */
@Data
public class UserIdReq {
    @ApiModelProperty(required = false, notes = "UserId")
    @NotNull
    private Long userId;
}