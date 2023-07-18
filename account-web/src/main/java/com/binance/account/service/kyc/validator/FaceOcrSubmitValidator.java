package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.security.IFace;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.inspector.common.enums.FaceOcrUploadType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

@Service
@Log4j2
public class FaceOcrSubmitValidator extends AbstractKycCertificateValidator<FaceOcrSubmitRequest> {

	@Resource
	private IFace iFace;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private UserKycMapper userKycMapper;

	@Override
	public void validateRequest(FaceOcrSubmitRequest req) {
		// 验证下类型是否正确
		String type = req.getType();
		if (StringUtils.isBlank(type)
				|| !StringUtils.equalsAny(type, FaceOcrUploadType.FACE.name(), FaceOcrUploadType.OCR.name())) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		FaceOcrUploadType uploadType = FaceOcrUploadType.valueOf(type);
		switch (uploadType) {
		case FACE:
			if (StringUtils.isBlank(req.getFace()) && StringUtils.isBlank(req.getFaceFileKey())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			break;
		case OCR:
			if (!StringUtils.isAllBlank(req.getFrontFileKey(), req.getBackFileKey())) {
				if (StringUtils.isAnyBlank(req.getFrontFileKey(), req.getBackFileKey())) {
					// 如果frontKey/backKey有一个不为空的情况下两个都需要存在
					throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
				}else {
					// 正面两个都存在了，可以直接验证通过，不需要继续验证
					break;
				}
			}
			if (!StringUtils.isAllBlank(req.getFront(), req.getBack())) {
				if (StringUtils.isAnyBlank(req.getFront(), req.getBack())) {
					// 如果front/back 存在一个，就需要两个都存在
					throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
				}else {
					break;
				}
			}else {
				// 证明了两组都不合符合要求的情况
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
		default:
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		if (StringUtils.isBlank(kycCertificate.getFaceOcrStatus())) {
			throw new BusinessException(AccountErrorCode.KYC_CANNOT_SUBMIT_CURRENT_STATUS);
		}
		// 企业用户不做ocr
		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			throw new BusinessException(AccountErrorCode.KYC_CANNOT_SUBMIT_CURRENT_STATUS);
		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getFaceOcrStatus())
				|| KycCertificateStatus.REVIEW.name().equals(kycCertificate.getFaceOcrStatus())) {
			throw new BusinessException(AccountErrorCode.KYC_CANNOT_SUBMIT_CURRENT_STATUS);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
	}

	/**
	 * 校验提交次数
	 *
	 * @param userId
	 * @param type
	 */
	public void validateRequestCount(Long userId, FaceOcrUploadType type) {
		int configCount = apolloCommonConfig.getKycFaceOcrSubmitCount();
		int configTime = apolloCommonConfig.getKycFaceOcrSubmitTime();
		if (configCount <= 0 || configTime <= 0) {
			// 配置小于等于0时不限制
			return;
		}
		Date endTime = DateUtils.getNewUTCDate();
		Date startTime = DateUtils.add(endTime, Calendar.MINUTE, -configTime);
		int currentCount = iFace.getFaceOcrTimes(userId, type, startTime, endTime);
		if (currentCount > configCount) {
			log.warn("kyc face ocr 提交在{}分钟内达到{}次, userId:{}", configTime, currentCount, userId);
			throw new BusinessException(AccountErrorCode.KYC_FACE_OCR_SUBMIT_OUT_COUNT);
		}
	}

	/**
	 * 验证kyc
	 *
	 * @param userId
	 * @return
	 */
	public UserKyc validateAndGetUserKyc(Long userId, FaceOcrUploadType type) {
		UserKyc userKyc = userKycMapper.getLast(userId);
		if (userKyc == null) {
			log.info("get user kyc record fail. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 验证下状态是否正确
		KycStatus status = userKyc.getStatus();
		if (KycStatus.pending != status && KycStatus.basic != status) {
			throw new BusinessException(AccountErrorCode.KYC_CANNOT_SUBMIT_CURRENT_STATUS);
		}
		// 判断是否在上传ocr的状态
		if (!StringUtils.equalsAnyIgnoreCase(userKyc.getFaceOcrStatus(), IdCardOcrStatus.PROCESS.name(),
				IdCardOcrStatus.REFUSED.name())) {
			throw new BusinessException(AccountErrorCode.KYC_CANNOT_SUBMIT_CURRENT_STATUS);
		}
		this.validateRequestCount(userId, type);
		return userKyc;
	}

}
