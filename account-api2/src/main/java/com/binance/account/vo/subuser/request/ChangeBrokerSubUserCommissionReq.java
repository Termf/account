package com.binance.account.vo.subuser.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("ChangeBrokerSubUserCommissionReq")
@Data
public class ChangeBrokerSubUserCommissionReq {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "makerCommission")
    @NotNull
    private BigDecimal makerCommission;

    @ApiModelProperty(required = true, notes = "takerCommission")
    @NotNull
    private BigDecimal takerCommission;


    @ApiModelProperty(required = false, notes = "marginMakerCommission")
    private BigDecimal marginMakerCommission;

    @ApiModelProperty(required = false, notes = "marginTakerCommission")
    private BigDecimal marginTakerCommission;
}