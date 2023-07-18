package com.binance.account.vo.certificate.request;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncRiskRatingCardCountryRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8739376673496290416L;
	
	@ApiModelProperty(required = true, notes = "用户id")
	private Long userId;
	
	@ApiModelProperty(required = true, notes = "渠道编号")
	private UserRiskRatingChannelCode channelCode;
	
	@ApiModelProperty(required = true, notes = "发卡归属国")
	private String cardCountry;

}
