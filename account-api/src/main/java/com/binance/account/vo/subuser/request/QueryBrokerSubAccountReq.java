package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("QueryBrokerSubAccountReq")
@Data
public class QueryBrokerSubAccountReq {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = false, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "页码，从1开始")
    private Integer page;

    @ApiModelProperty(required = false, notes = "每一页数量")
    private Integer size;
}
