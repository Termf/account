package com.binance.account.service.kyc.executor;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.kyc.validator.FaceOcrSubmitValidator;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.FaceOcrSubmitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.FaceOcrUploadType;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.vo.faceid.response.IdCardOcrResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.WebUtils;
import com.binance.messaging.common.utils.UUIDUtils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;

@Log4j2
@Service
public class FaceOcrSubmitExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private IFace iFace;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private FaceOcrSubmitValidator validator;
	@Resource
	private IUserFace iUserFace;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		FaceOcrSubmitRequest request = (FaceOcrSubmitRequest) kycFlowRequest;
		validator.validateApiRequest(request);
		Long userId = request.getUserId();

		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}

		// 老逻辑
		if (kycCertificate == null) {
			return executeOld(request);
		}
		// 新逻辑
		return executeNew(request, kycCertificate);

	}

	private KycFlowResponse executeNew(FaceOcrSubmitRequest request, KycCertificate kycCertificate) {
		Long userId = request.getUserId();
		FaceOcrUploadType type = FaceOcrUploadType.valueOf(request.getType());

		validator.validateKycCertificateStatus(kycCertificate);
		validator.validateRequestCount(userId, type);

		log.info("user uplaod face ocr image then begin validate, userId:{} type:{} language: {}", userId, type,
				WebUtils.getHeader("lang"));

		IdCardOcrResponse response = iFace.faceOcrValidate(request);
		if (response == null || StringUtils.isBlank(response.getStatus())) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		FaceOcrSubmitResponse submitResponse = new FaceOcrSubmitResponse();
		submitResponse.setFlowDefine(kycCertificate.getFlowDefine());
		IdCardOcrStatus ocrStatus = IdCardOcrStatus.valueOf(response.getStatus());

		//如果状态为review 包装为pass 前端可以继续做face
		if(IdCardOcrStatus.REVIEW.equals(ocrStatus)) {
			submitResponse.setStatus(IdCardOcrStatus.PASS.name());
		}else {
			submitResponse.setStatus(ocrStatus.name());
		}
		submitResponse.setMessage(
				MessageMapHelper.getMessage(response.getMessage(), WebUtils.getAPIRequestHeader().getLanguage()));

		kycCertificate.setFaceOcrStatus(ocrStatus.name());
		kycCertificate.setFaceOcrTips(response.getMessage());

		if(IdCardOcrStatus.PASS.equals(ocrStatus)) {
			kycCertificate.setBaseFillStatus(ocrStatus.name());
		}
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());

		FaceFlowInitResult faceFlowInitResult = null;
		if (IdCardOcrStatus.PASS == ocrStatus || IdCardOcrStatus.REVIEW == ocrStatus) {
			log.info("face ocr is pass/review userId:{}", userId);
			// 保存人脸识别的图片
			iFace.saveFaceReferenceCheckImage(userId, response.getFace(), response.getFaceCheck(), "FACE_OCR");
			// 创建人脸识别流程 (@TODO 老流程切换到新流程后 走faceInitExecutor。配成组合模式)
			log.info("idcard ocr success then begin init face flow. userId:{}", userId);
			faceFlowInitResult = iUserFace.initFaceFlowByTransId(UUIDUtils.getId(), userId,
					FaceTransType.KYC_USER, false, true);
			if (faceFlowInitResult == null) {
				log.error("KYC O认证FACE => 建立人脸识别流成失败. userId:{}", userId);
			}else {
				submitResponse.setTransId(faceFlowInitResult.getTransId());
				submitResponse.setFaceTransType(FaceTransType.KYC_USER.getCode());
			}

			KycFillInfo record = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

			if (StringUtils.isNotBlank(response.getName())) {
				record.setFirstName(response.getName());
				record.setMiddleName(null);
				record.setLastName(null);
			}
			if (StringUtils.isNotBlank(response.getBirthday())) {
				record.setBirthday(response.getBirthday());
			}
			kycFillInfoMapper.updateNameByUk(record);
			kycCertificate.setFaceStatus(KycCertificateStatus.PROCESS.name());
		}

		kycCertificateMapper.updateFaceOcrPassStatus(kycCertificate);
		KycFlowContext.getContext().setKycCertificate(kycCertificate);

		return submitResponse;
	}
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	private KycFlowResponse executeOld(FaceOcrSubmitRequest request) {
		Long userId = request.getUserId();
		FaceOcrUploadType type = FaceOcrUploadType.valueOf(request.getType());
		UserKyc userKyc = validator.validateAndGetUserKyc(userId, type);
		log.info("user uplaod face ocr image then begin validate, userId:{} type:{} language: {}", userId, type,
				WebUtils.getHeader("lang"));
		IdCardOcrResponse response = iFace.faceOcrValidate(request);
		if (response == null || StringUtils.isBlank(response.getStatus())) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		FaceOcrSubmitResponse submitResponse = new FaceOcrSubmitResponse();
		IdCardOcrStatus ocrStatus = IdCardOcrStatus.valueOf(response.getStatus());
		submitResponse.setStatus(ocrStatus.name());
		submitResponse.setMessage(
				MessageMapHelper.getMessage(response.getMessage(), WebUtils.getAPIRequestHeader().getLanguage()));
		userKyc.setFaceOcrStatus(ocrStatus.name());
		userKyc.setFaceOcrRemark(response.getMessage());
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		if (IdCardOcrStatus.REFUSED == ocrStatus) {
			userKyc.setFailReason(response.getMessage());
		}

		if (IdCardOcrStatus.PASS == ocrStatus) {
			log.info("face ocr is pass userId:{}", userId);
			// 保存人脸识别的图片
			iFace.saveFaceReferenceCheckImage(userId, response.getFace(), response.getFaceCheck(), "FACE_OCR");
			// 创建人脸识别流程 (@TODO 老流程切换到新流程后 走faceInitExecutor。配成组合模式)
			log.info("idcard ocr success then begin init face flow. userId:{}", userId);
			FaceFlowInitResult faceFlowInitResult = iUserFace.initFaceFlowByTransId(userKyc.getId().toString(), userId,
					FaceTransType.KYC_USER, false, false);
			if (faceFlowInitResult == null) {
				log.error("KYC O认证FACE => 建立人脸识别流成失败. userId:{}", userId);
			}
			if (StringUtils.isNotBlank(response.getName())) {
				userKyc.getBaseInfo().setFirstName(response.getName());
                userKyc.getBaseInfo().setMiddleName(null);
				userKyc.getBaseInfo().setLastName(null);
			}
			if (StringUtils.isNotBlank(response.getBirthday())) {
				try {
					userKyc.getBaseInfo().setDob(DateUtils.formatter(response.getBirthday(), "yyyy-MM-dd"));
				} catch (ParseException e) {
					log.error("idcard ocr 更新ocr 返回生日异常 userId: {},birthday: {}", userId, response.getBirthday(), e);
				}
			}
		}
		userKycMapper.saveFaceOcrStatus(userKyc);
		KycFlowContext.getContext().setUserKyc(userKyc);
		return submitResponse;

	}

}
