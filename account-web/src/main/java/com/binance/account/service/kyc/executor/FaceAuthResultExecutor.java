package com.binance.account.service.kyc.executor;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.face.handler.CompanyKycFaceHandler;
import com.binance.account.service.face.handler.UserKycFaceHandler;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.security.IFace;
import com.binance.account.vo.kyc.request.FaceAuthRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Log4j2
@Service
public class FaceAuthResultExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private KycCertificateMapper kycCertificateMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private CompanyCertificateMapper companyCertificateMapper;

	@Resource
	private CompanyKycFaceHandler companyKycFaceHandler;

	@Resource
	private UserKycFaceHandler userKycFaceHandler;

	@Resource
	private IFace iFace;
	
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		KycFlowResponse response = new KycFlowResponse();
		response.setKycType(kycFlowRequest.getKycType());
		response.setUserId(userId);
		FaceAuthRequest authRequest = (FaceAuthRequest) kycFlowRequest;

		// 判断是否是 admin 审核请求跳过face 流程。
		// admin审核face 赋值faceStatus。inspector 结果通知赋值status
		if (authRequest.getFaceStatus() != null) {
			auditFaceSkip(authRequest);
			KycFlowContext.getContext().setKycFlowResponse(response);
			return response;
		}

		if (authRequest == null || authRequest.getStatus() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		// 获取kyc认证信息
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		if (kycCertificate == null) {
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}
		kycCertificate.setFaceTips(authRequest.getMessage());
		switch (authRequest.getStatus()) {
		case REVIEW:
			kycCertificate.setFaceStatus(KycCertificateStatus.REVIEW.name());
			break;
		case PASSED:
			kycCertificate.setFaceStatus(KycCertificateStatus.PASS.name());
			break;
		case EXPIRED:
		case FAIL:
			kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
			break;
		default:
			// do nothing
			return response;
		}
		log.info("KYC认证FACE状态变更: userId:{}, faceStatus: message:{}", userId, authRequest.getStatus(),
				authRequest.getMessage());
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		kycCertificateMapper.updateFaceStatus(kycCertificate);
		// 设置上下文信息
		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFlowResponse(response);

		return response;
	}

	/**
	 * 审核跳过人脸识别。
	 * jumio->pass
	 * face->不为 pass，skip
	 * userFaceReference 存在有效记录
	 * @param authRequest
	 */
	private void auditFaceSkip(FaceAuthRequest authRequest) {
		Long userId = authRequest.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (!KycCertificateStatus.SKIP.equals(authRequest.getFaceStatus())) {
			log.warn("Face审核状态不为Skip不允许审核. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}

		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getJumioStatus())
				&& !KycCertificateStatus.PASS.name().equals(kycCertificate.getFaceOcrStatus())) {
			log.warn("Face审核失败,Jumio/FaceOcr状态不为Pass. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getFaceStatus())
				|| KycCertificateStatus.SKIP.name().equals(kycCertificate.getFaceStatus())) {
			log.warn("Face审核失败,Face状态在PASS/SKIP.不允许审核. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}

		// 校验人脸对比照片是否可用

		UserFaceReference userFaceReference = iFace.getUserFaceByMasterBD(userId);

		if (userFaceReference == null
				|| StringUtils.isAllBlank(userFaceReference.getRefImage(), userFaceReference.getCheckImage())) {
			log.info("Face审核失败.获取当前用户的人脸识别对比照信息失败，不能审核人脸: userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
		kycCertificate.setFaceStatus(authRequest.getFaceStatus().name());
		kycCertificate.setFaceTips("Manual Skip");
		kycCertificate.setOperator(authRequest.getOperator());
		kycCertificateMapper.updateFaceStatus(kycCertificate);
		
		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		FaceTransType faceTransType = kycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
				: FaceTransType.KYC_COMPANY;
		
		TransactionFaceLog transactionFaceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
		if(transactionFaceLog != null && !TransFaceLogStatus.isEndStatus(transactionFaceLog.getStatus())) {
			transactionFaceLog.setStatus(TransFaceLogStatus.PASSED);
			transactionFaceLog.setFaceRemark("Manual Skip");
			transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
			transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
		}
		

		KycFlowContext.getContext().setKycCertificate(kycCertificate);
	}

}
