package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("CreateBrokerSubUserApiReq")
@Data
public class CreateBrokerSubUserApiReq {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "是否可以交易,true or false")
    @NotNull
    private Boolean canTrade;

    @ApiModelProperty(required = true, notes = "是否可以交易margin,true or false")
    private Boolean marginTrade;

    @ApiModelProperty(required = true, notes = "是否可以交易期货,true or false")
    private Boolean futuresTrade;
}