package com.binance.account.service.kyc;

import com.binance.master.commons.ToString;
import com.binance.account.vo.kyc.response.AddressInfoSubmitResponse;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.FaceOcrSubmitResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateCenterDispatcherParam<T> extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = 363455265485298573L;
	
	/**
	 * true 请求center.false自处理
	 */
	private boolean dispatcher;
	
	private T response;
}
