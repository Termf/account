package com.binance.account.vo.kyc.request;

import com.binance.account.common.enums.KycCertificateStatus;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("地址认证确认信息")
@Getter
@Setter
public class AddresAuthResultRequest extends KycFlowRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8187969677860173682L;

	private KycCertificateStatus addressStatus;

	private String addressTips;
}
