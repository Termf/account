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
@ApiModel("UpdateBrokerSubUserApiReq")
@Data
public class UpdateBrokerSubUserApiReq  {

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "apikey")
    @NotBlank
    private String subAccountApiKey;

    @ApiModelProperty(required = true, notes = "是否可以交易,true or false")
    @NotNull
    private Boolean canTrade;

    @ApiModelProperty(required = true, notes = "是否可以交易margin,true or false")
    @NotNull
    private Boolean marginTrade;

    @ApiModelProperty(required = true, notes = "是否可以交易期货,true or false")
    @NotNull
    private Boolean futuresTrade;
}