package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhao chenkai
 */
@ApiModel("检查母账号，并获取子账号信息")
@Getter
@Setter
public class CheckParentAndGetSubUserInfoListRequest {

    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "子账户开启状态,1:开启; 0:未开启")
    private Integer isSubUserEnabled;

}
