package com.binance.account.service.kyc.convert;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoGender;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.GetKycStatusResponse;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

public class KycCertificateConvertor {

	/**
	 * convert to GetKycStatusResponse
	 *
	 * @param kycCertificate
	 * @return
	 */
	public static GetKycStatusResponse convert2GetKycStatusResponse(KycCertificate kycCertificate,
			LanguageEnum language) {
		GetKycStatusResponse response = new GetKycStatusResponse();
		BeanUtils.copyProperties(kycCertificate, response);
		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		response.setBaseFillTips(MessageMapHelper.getMessage(kycCertificate.getBaseFillTips(), language));
		response.setAddressTips(MessageMapHelper.getMessage(kycCertificate.getAddressTips(), language));
		response.setJumioTips(MessageMapHelper.getMessage(kycCertificate.getJumioTips(), language));
		response.setFaceTips(MessageMapHelper.getMessage(kycCertificate.getFaceTips(), language));
		response.setGoogleFormTips(MessageMapHelper.getMessage(kycCertificate.getGoogleFormTips(), language));
		response.setMessageTips(MessageMapHelper.getMessage(kycCertificate.getMessageTips(), language));
		response.setKycType(kycType);
		response.setKycLevel(kycCertificate.getKycLevel() == null ? 0 : kycCertificate.getKycLevel());
		return response;
	}

	/**
	 * convert to vo
	 *
	 * @param kycCertificate
	 * @return
	 */
	public static KycCertificateVo convert2KycCertificateVo(KycCertificate kycCertificate) {
		if (kycCertificate == null) {
			return null;
		}
		KycCertificateVo vo = new KycCertificateVo();
		BeanUtils.copyProperties(kycCertificate, vo);
		vo.setKycLevel(kycCertificate.getKycLevel() == null ? 0 : kycCertificate.getKycLevel());
		vo.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
		return vo;
	}

	/**
	 * convert to BaseInfoResponse
	 *
	 * @param kycCertificate
	 * @param kycFillInfo
	 * @return
	 */
	public static BaseInfoResponse convert2BaseInfoResponse(KycCertificate kycCertificate, KycFillInfo kycFillInfo) {
		BaseInfoResponse response = new BaseInfoResponse();
		BeanUtils.copyProperties(kycFillInfo, response);
		if (KycFillType.BASE.name().equals(kycFillInfo.getFillType())) {
			response.setGender(KycFillInfoGender.getGender(kycFillInfo.getGender()));
		}
		response.setFillType(KycFillType.valueOf(kycFillInfo.getFillType()));
		response.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
		response.setBaseFillStatus(KycCertificateStatus.valueOf(kycCertificate.getBaseFillStatus()));
		response.setBaseFillTips(kycCertificate.getBaseFillTips());
		response.setBindMobile(kycCertificate.getBindMobile());
		response.setMobileCode(kycCertificate.getMobileCode());
		return response;
	}

	/**
	 * convert 2 KycCertificate
	 *
	 * @param request
	 * @return
	 */
	public static KycCertificate convert2KycCertificate(AddresAuthResultRequest request) {
		KycCertificate kycCertificate = new KycCertificate();
		kycCertificate.setUserId(request.getUserId());
		kycCertificate.setAddressStatus(request.getAddressStatus().name());
		kycCertificate.setAddressTips(request.getAddressTips());
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		return kycCertificate;
	}

	/**
	 * convert 2 KycCertificate
	 *
	 * @param baseInfoRequest
	 * @return
	 */
	public static KycCertificate convert2KycCertificate(BaseInfoRequest baseInfoRequest) {
		KycCertificate kycCertificate = new KycCertificate();
		kycCertificate.setUserId(baseInfoRequest.getUserId());
		kycCertificate.setKycType(baseInfoRequest.getKycType().getCode());
		kycCertificate.setKycLevel(KycCertificateKycLevel.L0.getCode());
		kycCertificate.setStatus(KycCertificateStatus.PROCESS.name());
		kycCertificate.setBaseFillStatus(KycCertificateStatus.PROCESS.name());
		kycCertificate.setCreateTime(DateUtils.getNewUTCDate());
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		// 企业base
		if (KycCertificateKycType.COMPANY.equals(baseInfoRequest.getKycType())) {
			kycCertificate.setGoogleFormStatus(KycCertificateStatus.REVIEW.name());
		}
		return kycCertificate;
	}

	/**
	 * convert 2 FillInfo
	 *
	 * @param request
	 * @return
	 */
	public static KycFillInfo convert2KycFillInfo(KycFillInfo kycFillInfo, AddressInfoSubmitRequest request,
			String fileName) {
		if (kycFillInfo == null) {
			kycFillInfo = new KycFillInfo();
		}
		BeanUtils.copyProperties(request, kycFillInfo);
		kycFillInfo.setFillType(KycFillType.ADDRESS.name());
		kycFillInfo.setBillFile(fileName);
		kycFillInfo.setStatus("PROCESS");
		kycFillInfo.setSource(request.getSource().getCode());
		kycFillInfo.setCreateTime(DateUtils.getNewUTCDate());
		kycFillInfo.setUpdateTime(DateUtils.getNewUTCDate());
		return kycFillInfo;
	}

	/**
	 * convert 2 FillInfo
	 *
	 * @param kycFillInfo
	 * @param baseInfoRequest
	 * @return
	 */
	public static KycFillInfo convert2FillInfo(KycFillInfo kycFillInfo, BaseInfoRequest baseInfoRequest) {
		if (kycFillInfo == null) {
			kycFillInfo = new KycFillInfo();
		}
		BeanUtils.copyProperties(baseInfoRequest, kycFillInfo);
		if(StringUtils.isBlank(kycFillInfo.getResidenceCountry())) {
			kycFillInfo.setResidenceCountry(kycFillInfo.getCountry());
		}
		kycFillInfo.setFillType(KycFillType.BASE.name());
		kycFillInfo.setStatus("PROCESS");
		kycFillInfo.setCreateTime(DateUtils.getNewUTCDate());
		kycFillInfo.setUpdateTime(DateUtils.getNewUTCDate());
		// 个人base
		if (KycCertificateKycType.USER.equals(baseInfoRequest.getKycType()) && baseInfoRequest.getGender() != null) {
			kycFillInfo.setGender(baseInfoRequest.getGender().getGender());
		}
		kycFillInfo.setSource(baseInfoRequest.getSource().getCode());
		
		JSONObject ext = null;
		
		if(StringUtils.isNotBlank(baseInfoRequest.getTin())) {
			if(ext == null) {
				ext = new JSONObject();
				ext.put("tin", baseInfoRequest.getTin());
			}
		}
		
		if(ext != null) {
			kycFillInfo.setExt(ext.toString());
		}
		return kycFillInfo;
	}
}
