package com.binance.account.vo.certificate.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("riskRating申报请求")
@Getter
@Setter
public class RiskRatingApplyRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -950305334790536176L;
	
	@ApiModelProperty("用户ID")
	@NotNull
	private Long userId;
	
	@ApiModelProperty("渠道编号")
	@NotNull
	private UserRiskRatingChannelCode channelCode;
	
	@ApiModelProperty("申报金额")
	private BigDecimal applyAmount;
	
	@ApiModelProperty("居住国")
	private String residenceCountry;
	
	@ApiModelProperty("卡绑定国")
	private String cardCountry;

	// 4Bill渠道需要传
	@ApiModelProperty("taxId")
	private String taxId;

}
