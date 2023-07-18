package com.binance.account.vo.user.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by zhangjiwnen on 2018/6/7.
 */
@ApiModel("删除用户Request")
@Data
public class DeleteUserRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

}