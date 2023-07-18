package com.binance.account.service.kyc.executor;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFLowExecutorHelper;
import com.binance.account.service.kyc.validator.FaceOcrAuditValidator;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.common.model.KycCertificateVo;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import com.binance.messaging.common.utils.UUIDUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FaceOcrAuditExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private FaceOcrAuditValidator faceOcrAuditValidator;

	@Resource
	private KycFLowExecutorHelper kycFLowExecutorHelper;

	@Resource
	private IFace iFace;

	@Resource
	private IUserFace iUserFace;

	@Resource
	private KycCertificateApi certificateApi;
	
	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		KycFlowResponse response = new KycFlowResponse();

		if (kycFlowRequest == null) {
			return response;
		}

		FaceOcrAuthRequest authRequest = (FaceOcrAuthRequest) kycFlowRequest;
		faceOcrAuditValidator.validateApiRequest(authRequest);

		IdCardOcrStatus ocrStatus = IdCardOcrStatus.valueOf(authRequest.getStatus());

		Long userId = kycFlowRequest.getUserId();
		response.setKycType(kycFlowRequest.getKycType());
		response.setUserId(userId);

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		faceOcrAuditValidator.validateKycCertificateStatus(kycCertificate);

		// 如果审核状态和当前状态相同，并且为PASS直接修改用户姓名
		if (ocrStatus.name().equals(kycCertificate.getFaceOcrStatus())) {
			if (IdCardOcrStatus.PASS == ocrStatus) {
				syncPassUseBaseInfo(kycCertificate, response, userId, authRequest);
			}
			return response;
		}

		// 判断是否是ocr流程，如果是则同步状态，如果是jumio流程则修改基础信息
		boolean isOcrFlow = StringUtils.isNotBlank(kycCertificate.getFaceOcrStatus());

		// 非ocr流程，并且审核ocr通过。
		if (!isOcrFlow) {
			if (IdCardOcrStatus.PASS == ocrStatus) {
				syncPassUseBaseInfo(kycCertificate, response, userId, authRequest);
			}
			return response;
		}

		// ocr 流程同步状态
		KycCertificateStatus oldStatus = KycCertificateStatus.valueOf(kycCertificate.getFaceOcrStatus());

		kycCertificate.setFaceOcrStatus(ocrStatus.name());
		kycCertificate.setFaceOcrTips(authRequest.getMessage());
		kycCertificate.setBaseFillStatus(ocrStatus.name());

		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());

		FaceFlowInitResult faceFlowInitResult = null;
		if (IdCardOcrStatus.PASS == ocrStatus) {
			log.info("face ocr auth is pass userId:{}", userId);
			// 保存人脸识别的图片
			iFace.saveFaceReferenceCheckImage(userId, authRequest.getFace(), authRequest.getFaceCheck(), "FACE_OCR");

			log.info("idcard ocr success then begin init face flow. userId:{}", userId);
			faceFlowInitResult = iUserFace.initFaceFlowByTransId(UUIDUtils.getId(), userId, FaceTransType.KYC_USER,
					false, true);
			if (faceFlowInitResult == null) {
				log.error("KYC O认证FACE => 建立人脸识别流成失败. userId:{}", userId);
			}

			KycFillInfo record = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
			
			needUploadWck(record, authRequest);
			
			if (StringUtils.isNotBlank(authRequest.getName())) {
				record.setFirstName(authRequest.getName());
				record.setMiddleName(null);
				record.setLastName(null);
			}
			if (StringUtils.isNotBlank(authRequest.getBirthday())) {
				record.setBirthday(authRequest.getBirthday());
			}
			kycFillInfoMapper.updateNameByUk(record);
		}

		kycCertificateMapper.updateStatus(kycCertificate);

		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFlowResponse(response);

		kycFLowExecutorHelper.faceOcrChangeAfterHandler(kycCertificate, oldStatus);

		if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceOcrStatus())) {
			certificateToSendKycChangeMsg(kycCertificate);
		}
		return response;
	}

	private void syncPassUseBaseInfo(KycCertificate kycCertificate, KycFlowResponse response, Long userId,
			FaceOcrAuthRequest authRequest) {
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		KycFillInfo record = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

		needUploadWck(record, authRequest);
		
		if (StringUtils.isNotBlank(authRequest.getName())) {
			record.setFirstName(authRequest.getName());
			record.setMiddleName(null);
			record.setLastName(null);
		}
		if (StringUtils.isNotBlank(authRequest.getBirthday())) {
			record.setBirthday(authRequest.getBirthday());
		}

		kycFillInfoMapper.updateNameByUk(record);
		kycCertificateMapper.updateStatus(kycCertificate);
		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFlowResponse(response);
	}

	private void needUploadWck(KycFillInfo kycFillInfo, FaceOcrAuthRequest authRequest) {
		StringBuffer nameBuffered = new StringBuffer()
				.append(StringUtils.isBlank(kycFillInfo.getFirstName()) ? "" : kycFillInfo.getFirstName())
				.append(StringUtils.isBlank(kycFillInfo.getMiddleName()) ? "" : " " + kycFillInfo.getMiddleName())
				.append(StringUtils.isBlank(kycFillInfo.getLastName()) ? "" : " " + kycFillInfo.getLastName());
		KycFlowContext.getContext().setNeedWordCheck(!nameBuffered.toString().trim().equals(authRequest.getName()));

	}
	
	private void certificateToSendKycChangeMsg(KycCertificate kycCertificate) {
    	if(!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
    		return;
    	}
    	try {
    		KycCertificateVo certificate = new KycCertificateVo();
    		BeanUtils.copyProperties(kycCertificate, certificate);
    		certificate.setKycType(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType()) ? com.binance.certification.common.enums.KycCertificateKycType.COMPANY:
    			com.binance.certification.common.enums.KycCertificateKycType.USER);
    		certificateApi.sendBasicChangeMq(APIRequest.instance(certificate));
    	}catch(Exception e) {
    		log.warn("调用certificate发送kyc mq异常 userId:{}",kycCertificate.getUserId(),e);
    	}
    }
}
