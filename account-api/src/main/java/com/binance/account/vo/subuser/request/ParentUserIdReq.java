package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/11.
 */
@Data
public class ParentUserIdReq {

    @ApiModelProperty(required = false, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;
}