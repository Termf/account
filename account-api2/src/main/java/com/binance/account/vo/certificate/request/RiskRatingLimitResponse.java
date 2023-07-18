package com.binance.account.vo.certificate.request;

import java.math.BigDecimal;
import java.util.Map;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.util.BigDecimalToStringSerializer;
import com.binance.account.vo.certificate.response.UserRiskRatingTierLevelVo;
import com.binance.master.commons.ToString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModelProperty;
import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class RiskRatingLimitResponse extends ToString{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5209525643007453189L;

	@ApiModelProperty("状态")
	private UserRiskRatingStatus status;
	
	@ApiModelProperty("是否需要申报")
	private boolean needApply;
	
	@ApiModelProperty("kyc类型")
	private KycCertificateKycType kycType;
	
	@ApiModelProperty("tier等级")
	private UserRiskRatingTierLevel tierLevel;
	
	@ApiModelProperty("充值日限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal dailyLimit;
	
	@ApiModelProperty("充值月限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal monthlyLimit;

	@ApiModelProperty("充值年限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal yearlyLimit;

	@ApiModelProperty("提现日限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal withdrawDailyLimit;

	@ApiModelProperty("提现月限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal withdrawMonthlyLimit;

	@ApiModelProperty("提现年限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal withdrawYearlyLimit;

	@ApiModelProperty("充值总限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal totalLimit;

	@ApiModelProperty("提现总限额")
	@JsonSerialize(using = BigDecimalToStringSerializer.class)
	private BigDecimal withdrawTotalLimit;
	
	@ApiModelProperty("限额单位")
	private String limitUnit;
	
	@ApiModelProperty("风控tier等级对应限额")
	private Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> tierMap;
}
