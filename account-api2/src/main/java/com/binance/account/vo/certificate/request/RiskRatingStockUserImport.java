package com.binance.account.vo.certificate.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("渠道存量用户导入RiskRating")
@Getter
@Setter
public class RiskRatingStockUserImport extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7603752702906821913L;
	
	@ApiModelProperty("riskRatings")
	@NotNull
	private List<UserChannelRiskRatingVo> riskRatings;

}
