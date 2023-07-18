package com.binance.account.vo.certificate.request;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChannelRiskRatingRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4223070864270391950L;
	
	@ApiModelProperty(required = true, notes = "用户id")
	private Long userId;
	
	@ApiModelProperty(required = true, notes = "渠道编号")
	private UserRiskRatingChannelCode channelCode;

	@ApiModelProperty(required = false, notes = "自动创建申报记录 true 则创建")
	private Boolean autoApply;
	
}
