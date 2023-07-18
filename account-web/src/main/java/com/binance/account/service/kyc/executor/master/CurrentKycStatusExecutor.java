package com.binance.account.service.kyc.executor.master;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateNextStep;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.GetKycStatusResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioScanSource;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Log4j2
public class CurrentKycStatusExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private JumioApi jumioApi;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			GetKycStatusResponse response = new GetKycStatusResponse();
			response.setNextStep(KycCertificateNextStep.BASE);
			return response;
		}
		String lang = WebUtils.getHeader(Constant.LANG);
		LanguageEnum languageEnum = LanguageEnum.findByLang(lang);
		GetKycStatusResponse response = KycCertificateConvertor.convert2GetKycStatusResponse(kycCertificate,
				languageEnum);

		if (StringUtils.isBlank(kycCertificate.getBaseFillStatus())) {
			response.setNextStep(KycCertificateNextStep.BASE);
			return response;
		}

		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		if (baseInfo != null) {
			KycFillInfoVo baseVo = new KycFillInfoVo();
			BeanUtils.copyProperties(baseInfo, baseVo);
			response.setBase(baseVo);
		} else {
			response.setNextStep(KycCertificateNextStep.BASE);
			return response;
		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			// 如果user_channel_risk_rating 有值，这个地方需要用户显示地址认证模块
			List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(userId);

			if(ratings == null || ratings.isEmpty()) {
				return response;
			}
			boolean needAddress = false;
			for (UserChannelRiskRating userChannelRiskRating : ratings) {
				if(UserRiskRatingStatus.ENABLE.name().equals(userChannelRiskRating.getStatus())) {
					needAddress = true;
					break;
				}
			}

			response.setNeedAddress(needAddress);
			return response;
		}

		// base PROCESS、REFUSED 进入base
		if (!StringUtils.equalsAny(kycCertificate.getBaseFillStatus(), KycCertificateStatus.PASS.name(),
				KycCertificateStatus.REVIEW.name(), KycCertificateStatus.PROCESS.name())) {
			response.setNextStep(KycCertificateNextStep.BASE);
			return response;
		}
		// jumio 和face ocr 状态不会都为空，因为提交base的时候就已经决定 face 还是jumio
		String jumioStatus = kycCertificate.getJumioStatus();
		String faceOcrStatus = kycCertificate.getFaceOcrStatus();

		if (StringUtils.equalsAny(jumioStatus, KycCertificateStatus.REFUSED.name())) {
			response.setNextStep(KycCertificateNextStep.JUMIO);
			return response;
		}

		if (StringUtils.equalsAny(faceOcrStatus, KycCertificateStatus.REFUSED.name())) {
			response.setNextStep(KycCertificateNextStep.FACE_OCR);
			return response;
		}

		FaceTransType faceTransType = KycCertificateKycType
				.getByCode(kycCertificate.getKycType()) == KycCertificateKycType.USER ? FaceTransType.KYC_USER
						: FaceTransType.KYC_COMPANY;

		// jumio 为PROCESS 判断jumio 是否已经做了
		if (jumioStatus != null && StringUtils.equalsAny(jumioStatus, KycCertificateStatus.PROCESS.name(),
				KycCertificateStatus.REVIEW.name())) {
			APIResponse<JumioInfoVo> resp = jumioApi.getLastJumio(APIRequest.instance(new Long(userId)));
			if (resp.getStatus() != APIResponse.Status.OK) {
				log.error("查询inspector的jumio信息异常 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			JumioInfoVo jumioVo = resp.getData();
			// jumioVo 为空或者 scan_reference为空，代表jumio没做
			if (jumioVo == null || StringUtils.isBlank(jumioVo.getScanReference())) {
				response.setNextStep(KycCertificateNextStep.JUMIO);
				return response;
			}

			// 判断jumio 是否为初始。ps 存在再次做jumio
			if (jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.INIT
					|| jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.REFUED
					|| jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.ERROR
					|| jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.EXPIRED) {
				response.setNextStep(KycCertificateNextStep.JUMIO);
				return response;
			}

			boolean jumioIsKyc = JumioHandlerType.COMPANY_KYC.equals(jumioVo.getHandlerType())
					|| JumioHandlerType.USER_KYC.equals(jumioVo.getHandlerType());

			if (JumioScanSource.SDK.name().equalsIgnoreCase(jumioVo.getSource()) && jumioIsKyc) {
				return response;
			}

//			//jumio 非kyc流程，引导用户去做jumio，此时initjumio时，会创建face流程
//			if(!jumioIsKyc && jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.UPLOADED) {
//				TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), TransFaceLogStatus.INIT);
//				if(faceLog != null) {
//					response.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode() + ":"
//							+ faceLog.getTransId());
//					response.setFaceTransId(faceLog.getTransId());
//					response.setNextStep(KycCertificateNextStep.FACE);
//				}else {
//					//face已经通过
//					if(StringUtils.equalsAny(kycCertificate.getFaceStatus(), KycCertificateStatus.SKIP.name(),KycCertificateStatus.PASS.name())) {
//						return response;
//					}
//					response.setNextStep(KycCertificateNextStep.JUMIO);
//				}
//				return response;
//			}

			// 如果状态时process && jumio last记录不为认证 && kycCertificate 为非lock一条jumio的，返回jumio
			// 可能用户做了reset后创建了1条jumio，此时在sdk端做kyc的时候，提交base后，sdk会调用currentStatus后判断是否调用jumio
			// 此时jumio拿到的是reset的那条已经成功了的记录，但sdk端是需要用户在从新做jumio。
//			if (StringUtils.equalsAny(jumioStatus, KycCertificateStatus.PROCESS.name())) {
//				response.setNextStep(KycCertificateNextStep.JUMIO);
//				return response;
//			}
			
			//到这里 jumioStatus 必为 PASSED，REVIEW，UPLOADED
			if (StringUtils.equalsAny(jumioStatus, KycCertificateStatus.PROCESS.name())) {
				TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(),null);
				if(faceLog != null && (TransFaceLogStatus.INIT.equals(faceLog.getStatus())
						|| TransFaceLogStatus.PENDING.equals(faceLog.getStatus()))) {
					response.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode() + ":"
							+ faceLog.getTransId());
					response.setFaceTransId(faceLog.getTransId());
					response.setNextStep(KycCertificateNextStep.FACE);
					return response;
				}
//				else {
//				//face已经通过
//				if(StringUtils.equalsAny(kycCertificate.getFaceStatus(), KycCertificateStatus.SKIP.name(),KycCertificateStatus.PASS.name())) {
//					return response;
//				}
//				
//				response.setNextStep(KycCertificateNextStep.JUMIO);
//			}
				
				if(StringUtils.equalsAny(kycCertificate.getFaceStatus(), KycCertificateStatus.PASS.name())) {
					return response;
				}
				if(StringUtils.equalsAny(kycCertificate.getFaceStatus(), KycCertificateStatus.SKIP.name())) {
					if(jumioIsKyc) {
						return response;	
					}else {
						response.setNextStep(KycCertificateNextStep.JUMIO);
						return response;
					}
				}
				//最后一笔jumio 不是kyc 则去做jumio。通过initJumio来同步jumio最新状态
				if(jumioIsKyc) {
					return response;
				}else {
					response.setNextStep(KycCertificateNextStep.JUMIO);
					return response;
				}
			}
			
		}

		if (faceOcrStatus != null && StringUtils.equalsAny(faceOcrStatus, KycCertificateStatus.PROCESS.name(),
				KycCertificateStatus.REVIEW.name())) {

			if (StringUtils.equalsAny(faceOcrStatus, KycCertificateStatus.PROCESS.name())) {
				response.setNextStep(KycCertificateNextStep.FACE_OCR);
				return response;
			}

//			if (StringUtils.equalsAny(faceOcrStatus, KycCertificateStatus.REVIEW.name())) {
//				return response;
//			}

		}
		boolean isOcrFlow = StringUtils.isNotBlank(faceOcrStatus);
		String faceStatus = kycCertificate.getFaceStatus();


		// 正在进行中的情况，看下是否正在进行人脸识别
		if (StringUtils.isBlank(faceStatus)) {
			// 个人用户设置QRcode
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
			if (faceLog != null && isOcrFlow) {
				response.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode() + ":"
						+ faceLog.getTransId());
				response.setFaceTransId(faceLog.getTransId());
				response.setNextStep(KycCertificateNextStep.FACE);
			}
			return response;
		}

		if (StringUtils.equalsAny(faceStatus, KycCertificateStatus.PROCESS.name(),
				KycCertificateStatus.REFUSED.name())) {
			if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
				response.setNextStep(KycCertificateNextStep.FACE);
				return response;
			}
			// 个人用户设置QRcode
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
			if (faceLog != null) {
				response.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode() + ":"
						+ faceLog.getTransId());
				response.setFaceTransId(faceLog.getTransId());
				response.setNextStep(KycCertificateNextStep.FACE);

			}
		}

		return response;
	}

}
