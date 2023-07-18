package com.binance.account.vo.certificate.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRatingChangeLimitRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6489051530381165869L;
	
	@ApiModelProperty(required = true, notes = "id")
	@NotNull
	private Integer id;
	
	@ApiModelProperty(required = true, notes = "用户ID")
	@NotNull
    private Long userId;

	@ApiModelProperty(required = true, notes = "渠道编号")
	@NotNull
    private String channelCode;

	@ApiModelProperty(required = true, notes = "充值日限额")
	@NotNull
    private BigDecimal dailyLimit;
	
	@ApiModelProperty(required = true, notes = "充值月限额")
	@NotNull
    private BigDecimal monthlyLimit;

	@ApiModelProperty(required = true, notes = "充值年限额")
	@NotNull
	private BigDecimal yearlyLimit;

	@ApiModelProperty(required = true, notes = "充值总限额")
	@NotNull
    private BigDecimal totalLimit;

	@ApiModelProperty(required = true, notes = "提现日限额")
	@NotNull
	private BigDecimal withdrawDailyLimit;

	@ApiModelProperty(required = true, notes = "提现月限额")
	@NotNull
	private BigDecimal withdrawMonthlyLimit;

	@ApiModelProperty(required = true, notes = "提现年限额")
	@NotNull
	private BigDecimal withdrawYearlyLimit;

	@ApiModelProperty(required = true, notes = "提现总限额")
	@NotNull
	private BigDecimal withdrawTotalLimit;

}
