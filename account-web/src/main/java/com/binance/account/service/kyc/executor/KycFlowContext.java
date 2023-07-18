package com.binance.account.service.kyc.executor;

import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.service.kyc.KycFlowType;
import com.binance.account.vo.kyc.response.KycFlowResponse;

public class KycFlowContext {
	
	private KycCertificate kycCertificate;
	
	private KycFillInfo kycFillInfo;

	private KycFlowResponse kycFlowResponse;
	
	private UserKyc userKyc;
	
	private CompanyCertificate companyCertificate;
	
	private KycFlowType kycFlowType;
	
	private String front;
	
	private String back;
	
	private String face;
	
	private String idCardNumber;
	
	private boolean needWordCheck = false;
	
	
	private static final ThreadLocal<KycFlowContext> LOCAL = new ThreadLocal<KycFlowContext>() {
		@Override
		protected KycFlowContext initialValue() {
			return new KycFlowContext();
		}
	};
	
	public static KycFlowContext getContext() {
		return LOCAL.get();
	}
	
	public static void clean() {
		LOCAL.remove();
	}

	public KycCertificate getKycCertificate() {
		return kycCertificate;
	}

	public void setKycCertificate(KycCertificate kycCertificate) {
		this.kycCertificate = kycCertificate;
	}

	public KycFillInfo getKycFillInfo() {
		return kycFillInfo;
	}

	public void setKycFillInfo(KycFillInfo kycFillInfo) {
		this.kycFillInfo = kycFillInfo;
	}

	public KycFlowResponse getKycFlowResponse() {
		return kycFlowResponse;
	}

	public void setKycFlowResponse(KycFlowResponse kycFlowResponse) {
		this.kycFlowResponse = kycFlowResponse;
	}

	public UserKyc getUserKyc() {
		return userKyc;
	}

	public void setUserKyc(UserKyc userKyc) {
		this.userKyc = userKyc;
	}

	public CompanyCertificate getCompanyCertificate() {
		return companyCertificate;
	}

	public void setCompanyCertificate(CompanyCertificate companyCertificate) {
		this.companyCertificate = companyCertificate;
	}

	public KycFlowType getKycFlowType() {
		return kycFlowType;
	}

	public void setKycFlowType(KycFlowType kycFlowType) {
		this.kycFlowType = kycFlowType;
	}

	public String getFront() {
		return front;
	}

	public void setFront(String front) {
		this.front = front;
	}

	public String getBack() {
		return back;
	}

	public void setBack(String back) {
		this.back = back;
	}

	public String getFace() {
		return face;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public String getIdCardNumber() {
		return idCardNumber;
	}

	public void setIdCardNumber(String idCardNumber) {
		this.idCardNumber = idCardNumber;
	}

	public boolean isNeedWordCheck() {
		return needWordCheck;
	}

	public void setNeedWordCheck(boolean needWordCheck) {
		this.needWordCheck = needWordCheck;
	}

	
}
