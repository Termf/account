package com.binance.account.vo.kyc.response;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoGender;
import com.binance.account.common.enums.KycFillType;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("基础信息提交信息")
@Getter
@Setter
public class BaseInfoResponse extends KycFlowResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -766256667509737962L;

	private KycFillType fillType;

	private String firstName;

	private String middleName;

	private String lastName;

	private KycFillInfoGender gender;

	private String birthday;

	private String country;

	private String regionState;
	
	private String regionStateCode;

	private String city;

	private String address;

	private String postalCode;

	private KycCertificateStatus baseFillStatus;

	private String baseFillTips;

	private String taxId;

	private String nationality;

	private String companyName;

	private String companyAddress;

	private String contactNumber;

	private String registerName;

	private String registerEmail;
	
	private String bindMobile;
	
	private String mobileCode;
	
	private String email;

	private String authorizationToken;
}
