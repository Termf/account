package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/6/1.
 */
@ApiModel(description = "设置用户交易费Request", value = "设置用户交易费Request")
@Data
public class SetCommissionRequest implements Serializable {

    private static final long serialVersionUID = 5384171463721072781L;

    @ApiModelProperty(required = true, notes = "用戶Id")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "买方交易手续费")
    @NotNull
    private BigDecimal buyerCommission;

    @ApiModelProperty(required = true, notes = "卖方交易手续费")
    @NotNull
    private BigDecimal sellerCommission;

    @ApiModelProperty(required = true, notes = "主动方手续费")
    @NotNull
    private BigDecimal takerCommission;

    @ApiModelProperty(required = true, notes = "被动方手续费")
    @NotNull
    private BigDecimal makerCommission;

    @ApiModelProperty(required = true, notes = "修改原因")
    private String modifyReason;

    @ApiModelProperty(required = true, notes = "ExpectedRestoreTime")
    private String expectedRestoreTime;

}
