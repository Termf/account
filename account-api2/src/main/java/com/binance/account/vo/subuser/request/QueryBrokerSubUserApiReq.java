package com.binance.account.vo.subuser.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("QueryBrokerSubUserApiReq")
@Data
public class QueryBrokerSubUserApiReq {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "apikey")
    private String subAccountApiKey;
}