package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("CreateBrokerSubUserReq")
@Data
public class CreateBrokerSubUserReq {


	@ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账号备注")
    private String remark;
}