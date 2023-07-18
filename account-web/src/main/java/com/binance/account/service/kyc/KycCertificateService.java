package com.binance.account.service.kyc;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.common.query.KycRefByNumberQuery;
import com.binance.account.common.query.KycRefQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IKycCertificate;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.kyc.executor.us.KycBindMobileExecutor;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.response.KycRefQueryByNumberResponse;
import com.binance.account.vo.certificate.response.KycRefQueryResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoHistoryVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.AdditionalInfoRequest;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.request.DeleteKycNumberInfoRequest;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.kyc.request.FiatKycSyncStatusRequest;
import com.binance.account.vo.kyc.request.GetBaseInfoRequest;
import com.binance.account.vo.kyc.request.JumioAuthRequest;
import com.binance.account.vo.kyc.request.KycAccountChangeRequest;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.KycBindMobileRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.request.KycPassWithdrawFaceRequest;
import com.binance.account.vo.kyc.response.AddressInfoSubmitResponse;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.DeleteKycNumberInfoResponse;
import com.binance.account.vo.kyc.response.FaceOcrSubmitResponse;
import com.binance.account.vo.kyc.response.GetKycStatusResponse;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
@Log4j2
public class KycCertificateService {

	@Resource
	KycBindMobileExecutor kycBindMobileExecutor;

	@Resource
	private KycFlowProcessFactory kycFlowProcessFactory;

	@Resource
	private IKycCertificate iKycCertificate;

	@Resource
	private CertificateCenterDispatcher certificateCenterDispatcher;

	@Resource
	private IUserCertificate iUserCertificate;
	@Resource
	private IMsgNotification iMsgNotification;


	public BaseInfoResponse baseInfoSubmit(BaseInfoRequest request) {
		Long userId = request.getUserId();

		// kyc迁移
		CertificateCenterDispatcherParam<BaseInfoResponse> param = certificateCenterDispatcher.baseInfoSubmit(request);
		if (param.isDispatcher()) {
			return param.getResponse();
		}

		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					BaseInfoResponse response = (BaseInfoResponse) kycFlowProcessFactory
							.getProcessor(KycFlowProcessor.PROCESSOR_BASE_INFO_SUBMIT).process(request);
					return response;
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init web user kyc get lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			log.info("kyc lock InterruptedException. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public AddressInfoSubmitResponse addressInfoSubmit(AddressInfoSubmitRequest request) {
		Long userId = request.getUserId();

		CertificateCenterDispatcherParam<AddressInfoSubmitResponse> param = certificateCenterDispatcher
				.addressInfoSubmit(request);
		if (param.isDispatcher()) {
			return param.getResponse();
		}

		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					AddressInfoSubmitResponse response = (AddressInfoSubmitResponse) kycFlowProcessFactory
							.getProcessor(KycFlowProcessor.PROCESSOR_ADDRESS_INIT_SUBMIT).process(request);
					return response;
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init web user kyc get lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			log.info("kyc lock InterruptedException. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	/**
	 * get kyc_certificate all status
	 *
	 * @param userId
	 * @return
	 */
	public GetKycStatusResponse getKycStatus(Long userId) {
		KycFlowRequest request = new KycFlowRequest();
		request.setUserId(userId);
		return (GetKycStatusResponse) kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_CURRENT_KYC_STATUS)
				.process(request);
	}

	/**
	 * get base.kyc_fill_info
	 *
	 * @param request
	 * @return
	 */
	public BaseInfoResponse getKycBaseInfo(GetBaseInfoRequest request) {
		return iKycCertificate.getKycBaseInfo(request);
	}

	/**
	 * send kyc sms
	 *
	 * @param request
	 */
	public void kycSendSmsCode(KycBindMobileRequest request) {
		kycBindMobileExecutor.sendBindMobileVerifyCode(request);
	}

	/**
	 * get kyc_certificate list
	 *
	 * @param query
	 * @return
	 */
	public SearchResult<KycCertificateVo> getKycCertificateList(KycCertificateQuery query) {
		return iKycCertificate.getKycCertificateList(query);
	}

	/**
	 * get kyc_certificate detail
	 *
	 * @param userId
	 * @return
	 */
	public KycCertificateVo getKycCertificateDetail(Long userId, boolean deepLoad) {
		return iKycCertificate.getKycCertificateDetail(userId, deepLoad);
	}

	/**
	 * get kyc_fill_info
	 *
	 * @param userId
	 * @param fillType
	 * @return
	 */
	public KycFillInfoVo getKycFillInfo(Long userId, KycFillType fillType) {
		return iKycCertificate.getKycFillInfo(userId, fillType);
	}

	/**
	 * get kyc fill histories
	 *
	 * @param userId
	 * @param fillType
	 * @return
	 */
	public List<KycFillInfoHistoryVo> getKycFillInfoHistories(Long userId, KycFillType fillType) {
		return iKycCertificate.getKycFillInfoHistories(userId, fillType);
	}

	public Boolean syncFiatPtStatus(FiatKycSyncStatusRequest request) {
		return iKycCertificate.syncFiatPtStatus(request);
	}

	public FaceOcrSubmitResponse kycFaceOcrSubmit(FaceOcrSubmitRequest request) {
		Long userId = request.getUserId();

		CertificateCenterDispatcherParam<FaceOcrSubmitResponse> param = certificateCenterDispatcher
				.kycFaceOcrSubmit(request);
		if (param.isDispatcher()) {
			return param.getResponse();
		}

		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_OCR_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					FaceOcrSubmitResponse response = (FaceOcrSubmitResponse) kycFlowProcessFactory
							.getProcessor(KycFlowProcessor.PROCESSOR_FACE_OCR_SUBMIT).process(request);
					return response;
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init user face ocr submit get lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			log.info("init user face ocr submit  lock InterruptedException. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public SearchResult<KycRefQueryResponse> kycRefQuery(KycRefQuery kycRefQuery) {
		return iKycCertificate.kycRefQuery(kycRefQuery);
	}

	public SearchResult<KycRefQueryByNumberResponse> kycRefQueryByNumber(KycRefByNumberQuery kycRefByNumberQuery) {
		return iKycCertificate.kycRefQueryByNumber(kycRefByNumberQuery);
	}

	public DeleteKycNumberInfoResponse deleteKycNumberInfo(DeleteKycNumberInfoRequest deleteKycNumberInfoRequest) {
		return iKycCertificate.deleteKycNumberInfo(deleteKycNumberInfoRequest);
	}

	public JumioInitResponse baseInfoSubmitWithJumio(BaseInfoRequest request) {
		Long userId = request.getUserId();

		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					JumioInitResponse response = (JumioInitResponse) kycFlowProcessFactory
							.getProcessor(KycFlowProcessor.PROCESSOR_BASE_INFO_SUBMIT_WITH_JUMIO).process(request);
					return response;
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init web user kyc get lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			log.info("kyc lock InterruptedException. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public void forceKycPassedToExpired(KycForceToExpiredRequest request, KycCertificate kycCertificate) {
		if (request == null || request.getUserId() == null || StringUtils.isBlank(request.getFailReason())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		Long userId = request.getUserId();
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			log.info("user kyc status unknown. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_STATUS_NOT_PASSED);
		}
		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			throw new BusinessException(GeneralCode.SYS_ERROR, "不能重置过期企业认证");
		}
		// Jumio 流程才会触发
		if (!StringUtils.isNotBlank(kycCertificate.getJumioStatus())) {
			throw new BusinessException(GeneralCode.SYS_ERROR, "当前用户是OCR流程");
		}

		// 新版本按用户处理的逻辑
		JumioAuthRequest jumioAuthRequest = new JumioAuthRequest();
		jumioAuthRequest.setJumioStatus(JumioStatus.EXPIRED.name());
		jumioAuthRequest.setMessage(request.getFailReason());
		jumioAuthRequest.setUserId(request.getUserId());
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_JUMIO_AUTH_RESULT).process(jumioAuthRequest);
	}

	public UserKycCountryResponse getKycCountry(Long userId) throws Exception {
		return iKycCertificate.getKycCountry(userId);
	}

	public KycFillInfoVo additionalInfo(AdditionalInfoRequest request) {
		return iKycCertificate.additionalInfo(request);
	}

	public JumioInitResponse kycJumioInit(KycFlowRequest request) {
		CertificateCenterDispatcherParam<JumioInitResponse>  param = certificateCenterDispatcher.kycJumioInit(request);
		if(param.isDispatcher()) {
			return param.getResponse();
		}
		JumioInitResponse response = (JumioInitResponse) kycFlowProcessFactory
				.getProcessor(KycFlowProcessor.PROCESSOR_KYC_JUMIO_INIT).process(request);
		return response;
	}

	public void addressAuthResult(AddresAuthResultRequest request) {
		CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.addressAuthResult(request);
		if(param.isDispatcher()) {
			return;
		}
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_ADDRESS_AUTH_RESULT).process(request);
	}

    public void changeAccountStatusAndLevel(KycAccountChangeRequest request) {
		log.info("kyc change account status and level {}", JSON.toJSONString(request));
		Long userId = request.getUserId();
		Boolean isPass = request.getIsPass();
		Integer securityLevel = request.getSecurityLevel();
		if (userId ==  null || securityLevel == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if(isPass != null) {
			int statusChange = iUserCertificate.updateCertificateStatus(userId, isPass);
			log.info("kyc change user status by userId:{} result:{}", userId, statusChange);
		}
		int row = iUserCertificate.updateSecurityLevel(userId, securityLevel);
		log.info("kyc change user security by userId:{} result:{}", userId, row);
		Map<String, Object> dataMsg = new HashMap<>();
		dataMsg.put("userId", userId);
		dataMsg.put("level", securityLevel);
		MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
		log.info("KYC change notify pnk web iMsgNotification security level:{}", JSON.toJSONString(msg));
		iMsgNotification.send(msg);
    }

	public void changeWithdrawFaceByKycPass(KycPassWithdrawFaceRequest request) {
		Long userId = request.getUserId();
		log.info("kyc pass check withdraw security face userId:{}", userId);
		FaceTransType transType = FaceTransType.valueOf(request.getTransType());
		String transId = request.getTransId();
		String refTransId = request.getRefTransId();
		String kycStatus = request.getKycStatus();
		Date kycPassDate = request.getKycPassTime();
		iUserCertificate.kycPassCheckSecurityFaceCheck(userId, transId, transType, refTransId, kycStatus, kycPassDate);
	}
	
	public void auditGoogleForm(KycAuditRequest request) {
		CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.auditGoogleForm(request);
		if(param.isDispatcher()) {
			return;
		}
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_GOOGLE_FORM_AUDIT).process(request);
	}
	
	public void auditFaceOcr(FaceOcrAuthRequest request) {
		CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.auditFaceOcr(request);
		if(param.isDispatcher()) {
			return;
		}
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_FACE_OCR_AUDIT).process(request);
		return;
	}
	
	public void auditBaseInfo(KycAuditRequest request) {
		CertificateCenterDispatcherParam<Void> param = certificateCenterDispatcher.auditBaseInfo(request);
		if(param.isDispatcher()) {
			return;
		}
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_BASE_INFO_AUDIT).process(request);
		return;
	}
}
