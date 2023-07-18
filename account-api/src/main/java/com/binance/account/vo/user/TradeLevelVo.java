package com.binance.account.vo.user;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lufei
 * @date 2018/11/16
 */
@Data
@ApiModel("交易等级详情")
public class TradeLevelVo extends ToString {

    private static final long serialVersionUID = -4687060276395481611L;

    @ApiModelProperty("主键")
    @NotNull(groups = Edit.class)
    private Long id;

    @ApiModelProperty("交易级别")
    @NotNull
    private Integer level;

    @ApiModelProperty("BNB最低持仓额")
    @NotNull
    private BigDecimal bnbFloor;

    @ApiModelProperty("BNB最高持仓额")
    @NotNull
    private BigDecimal bnbCeil;

    @ApiModelProperty("BTC最低持仓数量")
    @NotNull
    private BigDecimal btcFloor;

    @ApiModelProperty("BTC最高持仓数量")
    @NotNull
    private BigDecimal btcCeil;

    @ApiModelProperty("被动方手续费")
    @NotNull
    private BigDecimal makerCommission;

    @ApiModelProperty("主动方手续费")
    @NotNull
    private BigDecimal takerCommission;

    @ApiModelProperty("买方交易手续费")
    @NotNull
    private BigDecimal buyerCommission;

    @ApiModelProperty("卖方交易手续费")
    @NotNull
    private BigDecimal sellerCommission;
}
