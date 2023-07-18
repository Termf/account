package com.binance.account.vo.user.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/6/4.
 */
@ApiModel("修改用户Request")
@Data
public class ModifyUserRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("是否特殊用户")
    @NotNull
    private Boolean isSpecialUser;

    @ApiModelProperty("是否种子用户")
    @NotNull
    private Boolean isSeedUser;

    @ApiModelProperty("是否重置手机号")
    @NotNull
    private Boolean isResetMobileNo;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("用户邮箱")
    private String email;
}
