package com.binance.account.vo.user.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("修改用户昵称Request")
@Data
public class UpdateNickNameRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("昵称")
    @NotNull
    private String nickName;

}
