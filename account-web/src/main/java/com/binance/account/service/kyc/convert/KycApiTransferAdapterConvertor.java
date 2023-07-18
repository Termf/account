package com.binance.account.service.kyc.convert;

import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoRefType;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.kyc.JumioVo;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.request.KycBaseInfoRequest;
import com.binance.account.vo.user.response.InitSdkUserKycResponse;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.utils.DateUtils;

public class KycApiTransferAdapterConvertor {

	/**
	 * 个人baseInfo提交转换新流程request
	 * 
	 * @param request
	 * @return
	 */
	public static BaseInfoRequest convert2UserBaseInfoRequest(KycBaseInfoRequest request, TerminalEnum source) {
		BaseInfoRequest req = new BaseInfoRequest();
		UserKycVo.BaseInfo baseInfo = request.getBaseInfo();
		Long userId = request.getUserId();
		req.setUserId(userId);
		req.setKycType(KycCertificateKycType.USER);
		req.setFirstName(baseInfo.getFirstName());
		req.setLastName(baseInfo.getLastName());
		req.setCity(baseInfo.getCity());
		req.setCountry(baseInfo.getCountry());
		req.setSource(source);
		req.setAddress(baseInfo.getAddress());
		req.setNationality(baseInfo.getNationality());
		if (baseInfo.getDob() != null) {
			req.setBirthday(DateUtils.formatter(baseInfo.getDob(), "yyyy-MM-dd"));
		}
		req.setPostalCode(baseInfo.getPostalCode());
		return req;
	}

	/**
	 * 企业baseInfo提交转换新流程request
	 * 
	 * @param request
	 * @param source
	 * @return
	 */
	public static BaseInfoRequest convert2CompanyBaseInfoRequest(SaveCompanyCertificateRequest request,
			TerminalEnum source) {
		BaseInfoRequest req = new BaseInfoRequest();
		req.setUserId(request.getUserId());
		req.setKycType(KycCertificateKycType.COMPANY);
		req.setCompanyName(request.getCompanyName());
		req.setCountry(request.getCompanyCountry());
		req.setRegisterName(request.getApplyerName());
		req.setRegisterEmail(request.getApplyerEmail());
		req.setContactNumber(request.getContactNumber());
		req.setSource(source);
		return req;
	}

	/**
	 * web端 jumio convert
	 * 
	 * @param response
	 * @return
	 */
	public static JumioTokenResponse convert2JumioTokenResponse(JumioInitResponse response) {
		JumioTokenResponse resp = new JumioTokenResponse();
		resp.setAuthorizationToken(response.getRedirectUrl());
		return resp;
	}

	/**
	 * sdk端 jumio convert
	 * 
	 * @param response
	 * @return
	 */
	public static InitSdkUserKycResponse convert2InitSdkUserKycResponse(JumioInitResponse response) {
		InitSdkUserKycResponse resp = new InitSdkUserKycResponse();
		resp.setApiKey(response.getApiKey());
		resp.setApiSecret(response.getApiSecret());
		resp.setMerchantReference(response.getMerchantReference());
		resp.setUserReference(response.getUserReference());
		resp.setCallBack(response.getCallBack());
		return resp;
	}

	public static UserKycVo convert2UserKycVo(KycCertificateVo kycCertificateVo,
			TransactionFaceLog transactionFaceLog) {
		UserKycVo userKycVo = new UserKycVo();
		userKycVo.setBaseInfo(new UserKycVo.BaseInfo());
		userKycVo.setCheckInfo(new UserKycVo.CheckInfo());

		if (kycCertificateVo == null) {
			return userKycVo;
		}
		userKycVo.setUserId(kycCertificateVo.getUserId());

		if (KycCertificateStatus.PASS.name().equals(kycCertificateVo.getStatus())) {
			userKycVo.setStatus(KycStatus.passed);
		} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificateVo.getStatus())) {
			userKycVo.setStatus(KycStatus.refused);
		} else if (KycCertificateStatus.FORBID_PASS.name().equals(kycCertificateVo.getStatus())) {
			userKycVo.setStatus(KycStatus.forbidPassed);
		} else {
			if (KycCertificateStatus.PASS.name().equals(kycCertificateVo.getJumioStatus())
					|| KycCertificateStatus.PASS.name().equals(kycCertificateVo.getFaceOcrStatus())) {
				userKycVo.setStatus(KycStatus.jumioPassed);
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificateVo.getJumioStatus())
					|| KycCertificateStatus.REFUSED.name().equals(kycCertificateVo.getFaceOcrStatus())) {
				userKycVo.setStatus(KycStatus.jumioRefused);
			} else {
				userKycVo.setStatus(KycStatus.pending);
			}
		}
		userKycVo.setCreateTime(kycCertificateVo.getCreateTime());
		userKycVo.setUpdateTime(kycCertificateVo.getUpdateTime());
		userKycVo.setFailReason(kycCertificateVo.getMessageTips());
		JumioVo jumioVo = kycCertificateVo.getJumioVo();
		if (jumioVo != null) {
			userKycVo.setCheckStatus(jumioVo.getJumioStatus());
			userKycVo.setScanReference(jumioVo.getScanReference());
			BeanUtils.copyProperties(jumioVo, userKycVo.getCheckInfo());
			com.binance.account.common.enums.JumioStatus jumioStatus = null;
			if (KycCertificateStatus.PASS.name().equals(kycCertificateVo.getJumioStatus())) {
				jumioStatus = com.binance.account.common.enums.JumioStatus.jumioPassed;
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificateVo.getJumioStatus())) {
				jumioStatus = com.binance.account.common.enums.JumioStatus.jumioRefused;
			}
			userKycVo.getCheckInfo().setStatus(jumioStatus);
			userKycVo.getCheckInfo().setAuthToken(jumioVo.getRedirectUrl());
		}

		if (transactionFaceLog != null) {
			userKycVo.setFaceStatus(transactionFaceLog.getFaceStatus());
			userKycVo.setFaceRemark(kycCertificateVo.getFaceOcrTips());
			userKycVo.setTransFaceLogId(transactionFaceLog.getId() + "");
		}
		if (StringUtils.isNotBlank(kycCertificateVo.getFaceOcrStatus())) {
			userKycVo.setFaceOcrStatus(kycCertificateVo.getFaceOcrStatus());
			userKycVo.setFaceOcrRemark(kycCertificateVo.getFaceOcrTips());

		}
		KycFillInfoVo base = kycCertificateVo.getBaseInfo();

		if (base != null) {
			userKycVo.getBaseInfo().setFirstName(base.getFirstName());
			userKycVo.getBaseInfo().setLastName(base.getLastName());
			userKycVo.getBaseInfo().setCity(base.getCity());
			userKycVo.getBaseInfo().setCountry(base.getCountry());
			userKycVo.getBaseInfo().setAddress(base.getAddress());
			userKycVo.getBaseInfo().setNationality(base.getNationality());
			try {
				userKycVo.getBaseInfo().setDob(DateUtils.formatter(base.getBirthday(), "yyyy-MM-dd"));
			} catch (ParseException e) {
			}
			userKycVo.getBaseInfo().setPostalCode(base.getPostalCode());
		}

		return userKycVo;
	}

	public static KycCertificate convert2KycCertificate(UserKyc userKyc) {
		KycCertificate kycCertificate = new KycCertificate();
		kycCertificate.setUserId(userKyc.getUserId());
		kycCertificate.setKycType(KycCertificateKycType.USER.getCode());
		kycCertificate.setKycLevel(KycCertificateKycLevel.L2.getCode());
		kycCertificate.setStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setCreateTime(userKyc.getCreateTime());
		kycCertificate.setUpdateTime(userKyc.getUpdateTime());
		kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
		if (StringUtils.isNotBlank(userKyc.getFaceOcrStatus())) {
			kycCertificate.setFaceOcrStatus(KycCertificateStatus.PASS.name());
		} else {
			kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
		}
		if (StringUtils.isNotBlank(userKyc.getFaceStatus())) {
			kycCertificate.setFaceStatus(KycCertificateStatus.PASS.name());
		} else {
			kycCertificate.setFaceStatus(KycCertificateStatus.SKIP.name());
		}
		kycCertificate.setLockOne(false);
		kycCertificate.setFlowDefine("master");
		return kycCertificate;
	}

	public static KycFillInfo convert2KycFillInfo(UserKyc userKyc, UserKycApprove userKycApprove) {
		KycFillInfo kycFillInfo = new KycFillInfo();
		kycFillInfo.setUserId(userKyc.getUserId());
		kycFillInfo.setFillType(KycFillType.BASE.name());
		kycFillInfo.setCreateTime(userKyc.getCreateTime());
		kycFillInfo.setUpdateTime(userKyc.getUpdateTime());
		kycFillInfo.setStatus(KycCertificateStatus.PROCESS.name());

		UserKyc.BaseInfo baseInfo = userKyc.getBaseInfo();
		UserKycApprove.BaseInfo approveBaseInfo = userKycApprove == null ? null : userKycApprove.getBaseInfo();
		if (baseInfo != null) {
			kycFillInfo.setFirstName(baseInfo.getFirstName());
			kycFillInfo.setMiddleName(baseInfo.getMiddleName());
			kycFillInfo.setLastName(baseInfo.getLastName());
			try {
				kycFillInfo.setBirthday(DateUtils.formatter(baseInfo.getDob(), "yyyy-MM-dd"));
			} catch (Exception e) {
			}
			kycFillInfo.setCity(baseInfo.getCity());
			kycFillInfo.setCountry(baseInfo.getCountry());
			kycFillInfo.setAddress(baseInfo.getAddress());
			kycFillInfo.setNationality(baseInfo.getNationality());
			kycFillInfo.setPostalCode(baseInfo.getPostalCode());
		} else if (approveBaseInfo != null) {
			kycFillInfo.setFirstName(approveBaseInfo.getFirstName());
			kycFillInfo.setMiddleName(approveBaseInfo.getMiddleName());
			kycFillInfo.setLastName(approveBaseInfo.getLastName());
			try {
				kycFillInfo.setBirthday(DateUtils.formatter(approveBaseInfo.getDob(), "yyyy-MM-dd"));
			} catch (Exception e) {
			}
			kycFillInfo.setCity(approveBaseInfo.getCity());
			kycFillInfo.setCountry(approveBaseInfo.getCountry());
			kycFillInfo.setAddress(approveBaseInfo.getAddress());
			kycFillInfo.setPostalCode(approveBaseInfo.getPostalCode());
		}
		if (StringUtils.isNotBlank(userKyc.getTransFaceLogId())) {
			kycFillInfo.setRefType(KycFillInfoRefType.WITHDRAW_FACE.name());
			kycFillInfo.setRefId(userKyc.getTransFaceLogId());
		}

		return kycFillInfo;
	}

	public static KycCertificate convert2KycCertificate(CompanyCertificate companyCertificate) {
		KycCertificate kycCertificate = new KycCertificate();
		kycCertificate.setUserId(companyCertificate.getUserId());
		kycCertificate.setKycType(KycCertificateKycType.COMPANY.getCode());
		kycCertificate.setKycLevel(KycCertificateKycLevel.L2.getCode());
		kycCertificate.setStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setCreateTime(companyCertificate.getInsertTime());
		kycCertificate.setUpdateTime(companyCertificate.getUpdateTime());
		kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setFaceStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setGoogleFormStatus(KycCertificateStatus.PASS.name());
		kycCertificate.setLockOne(false);
		kycCertificate.setFlowDefine("master");
		return kycCertificate;
	}

	public static KycFillInfo convert2KycFillInfo(CompanyCertificate companyCertificate) {
		KycFillInfo kycFillInfo = new KycFillInfo();
		kycFillInfo.setUserId(companyCertificate.getUserId());
		kycFillInfo.setFillType(KycFillType.BASE.name());
		kycFillInfo.setCreateTime(companyCertificate.getInsertTime());
		kycFillInfo.setUpdateTime(companyCertificate.getUpdateTime());
		kycFillInfo.setStatus(KycCertificateStatus.PROCESS.name());
		kycFillInfo.setCompanyName(companyCertificate.getCompanyName());
		kycFillInfo.setCompanyAddress(companyCertificate.getCompanyAddress());
		if(StringUtils.isNoneBlank(companyCertificate.getCompanyCountry()) && companyCertificate.getCompanyCountry().length()<3) {
			kycFillInfo.setCountry(companyCertificate.getCompanyCountry());
		}
		kycFillInfo.setRegisterName(companyCertificate.getApplyerName());
		kycFillInfo.setRegisterEmail(companyCertificate.getApplyerEmail());
		kycFillInfo.setContactNumber(companyCertificate.getContactNumber());
		if (StringUtils.isNotBlank(companyCertificate.getTransFaceLogId())) {
			kycFillInfo.setRefType(KycFillInfoRefType.WITHDRAW_FACE.name());
			kycFillInfo.setRefId(companyCertificate.getTransFaceLogId());
		}
		return kycFillInfo;
	}
}
