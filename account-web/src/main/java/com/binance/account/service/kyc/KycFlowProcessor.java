package com.binance.account.service.kyc;

import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;

/**
 * 处理器接口规范
 * 
 * @author liufeng
 *
 */
public interface KycFlowProcessor {

	/** kyc flow 配置文件中配置的处理器 */
	String PROCESSOR_BASE_INFO_SUBMIT = "base-info-submit";
	String PROCESSOR_IDM_AUTH_RESULT = "idm-auth-result";
	String PROCESSOR_ADDRESS_INIT_SUBMIT = "address-info-submit";
	String PROCESSOR_ADDRESS_AUTH_RESULT = "address-auth-result";
	String PROCESSOR_KYC_BIND_MOBILE = "kyc-bind-mobile";
	String PROCESSOR_KYC_JUMIO_INIT = "kyc-jumio-init";
	String PROCESSOR_JUMIO_AUTH_RESULT = "jumio-auth-result";
	String PROCESSOR_KYC_FACE_INIT = "kyc-face-init";
	String PROCESSOR_KYC_FACE_AUTH_RESULT = "kyc-face-auth-result";
	String PROCESSOR_BASE_INFO_AUDIT = "base-info-audit";
	String PROCESSOR_GOOGLE_FORM_AUDIT = "google-form-audit";
	String PROCESSOR_FACE_OCR_SUBMIT = "face-ocr-submit";
	String PROCESSOR_BASE_INFO_SUBMIT_WITH_JUMIO = "base-info-submit-with-jumio";
	String PROCESSOR_CURRENT_KYC_STATUS = "current-kyc-status";
	String PROCESSOR_FACE_OCR_AUDIT = "face-ocr-audit";
	
	/**
	 *
	 * @param kycFlowRequest
	 * @return
	 */
	public KycFlowResponse process(KycFlowRequest kycFlowRequest);

	/**
	 * 处理全局后置
	 * 
	 * @param kycFlowRequest
	 */
	public void processGlobalEnd(KycFlowRequest kycFlowRequest);

}
