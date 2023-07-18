package com.binance.account.service.kyc.executor.master;

import com.binance.account.common.enums.JumioStatus;
import com.binance.account.common.enums.JumioType;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.certificate.impl.UserCertificateBusiness;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFlowType;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.JumioInitMasterValidator;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.account.vo.user.response.JumioSdkInitResponse;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioScanSource;
import com.binance.inspector.util.InspectorErrorCode;
import com.binance.inspector.vo.jumio.response.InitJumioResponse;
import com.binance.inspector.vo.jumio.response.InitSdkJumioResponse;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Log4j2
@Service
public class JumioInitMasterExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private JumioInitMasterValidator validator;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	private JumioMapper jumioMapper;
	@Resource
	private UserCertificateBusiness userCertificateBusiness;
	@Resource
	private JumioBusiness jumioBusiness;

	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		if (KycFlowType.SKIP_JUMIO.equals(KycFlowContext.getContext().getKycFlowType())) {
			//跳过当前执行
			return KycFlowContext.getContext().getKycFlowResponse();
		}
		KycFlowContext.getContext().setKycFlowType(KycFlowType.INIT_JUMIO);
		validator.validateApiRequest(kycFlowRequest);

		Long userId = kycFlowRequest.getUserId();
		try {
			KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();

			if (kycCertificate == null) {
				kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
			}

			boolean isOldFlow = kycCertificate == null;
			// todo，出现了走老流程的情况，逻辑上不允许
			if (isOldFlow) {
				return executeOldFlow(kycFlowRequest);
			} else {
				return executeNewFlow(kycFlowRequest);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error(String.format("INIT jumio 处理异常 userId:%s", userId), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	private KycFlowResponse executeOldFlow(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		switch (kycFlowRequest.getKycType()) {
		case USER:
			UserKyc userKyc = KycFlowContext.getContext().getUserKyc();
			if (userKyc == null) {
				userKyc = userKycMapper.getLast(userId);
			}

			if (userKyc == null) {
				throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
			}

			if (userKyc.getStatus() != KycStatus.pending && userKyc.getStatus() != KycStatus.basic) {
				log.warn("当前用户kyc不允许初始化Jumio userId:{}", userId);
				throw new BusinessException(GeneralCode.USER_KYC_PENDING);
			}

			if (userKyc.isOcrFlow()) {
				log.warn("当前用户需要走face ocr 不允许初始化Jumio userId:{}", userId);
				throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
			}

			KycCertificate kycCertificate = new KycCertificate();
			kycCertificate.setUserId(userId);
			kycCertificate.setKycType(KycCertificateKycType.USER.getCode());
			validator.validateRequestCount(kycCertificate);

			boolean isSdk = Objects.equals(TerminalEnum.ANDROID, kycFlowRequest.getSource())
					|| Objects.equals(TerminalEnum.IOS, kycFlowRequest.getSource());

			KycFlowResponse response;
			if (isSdk) {
				response = initSdkJumio(userKyc);
			} else {
				response = initWebJumio(userKyc);
			}

			return response;
		case COMPANY:
			JumioInitResponse resp = new JumioInitResponse();
			CompanyCertificate companyCertificate = KycFlowContext.getContext().getCompanyCertificate();
			if (companyCertificate == null) {
				companyCertificate = this.companyCertificateMapper.getLast(userId);
			}

			if (companyCertificate == null) {
				throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
			}

			userCertificateBusiness.validateHadReviewKyc(companyCertificate.getUserId(), companyCertificate);

			if (StringUtils.isNotBlank(companyCertificate.getJumioId())) {
				Jumio jumio = jumioMapper.selectByPrimaryKey(userId, companyCertificate.getJumioId());
				if (jumio != null && jumio.getStatus() != JumioStatus.expired) {
					resp.setRedirectUrl("success");
					return resp;
				}
			}

			String bizId = String.valueOf(companyCertificate.getId());
			// 如果还没有jumio 信息，需要初始化JUMIO信息
			Jumio jumio = jumioBusiness.initWebJumio(userId,
					com.binance.inspector.common.enums.JumioHandlerType.COMPANY_KYC, bizId, false);
			log.info("save company kyc base info success, and init jumio. userId:{} bizId:{} success", userId, bizId);
			// jumio 生成后，更新jumio id 到 company certificate 表中
			companyCertificate.setJumioId(jumio.getId());
			companyCertificate.setScanReference(jumio.getScanReference());
			companyCertificate.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
			companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			companyCertificateMapper.saveJumioId(companyCertificate);

			resp.setRedirectUrl(jumio.getAuthToken());
			return resp;
		default:
			return new JumioInitResponse();
		}
	}

	private KycFlowResponse executeNewFlow(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}
		if (kycCertificate == null) {
			log.warn("KYC认证JUMIO => 获取不到用户KYC认证记录信息. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}

		validator.validateKycCertificateStatus(kycCertificate);
		validator.validateRequestCount(kycCertificate);

		KycCertificateKycType kycType = kycFlowRequest.getKycType();

		if (KycCertificateKycType.USER.equals(kycType)) {
			UserKyc userKyc = KycFlowContext.getContext().getUserKyc();
			if (userKyc == null) {
				userKyc = userKycMapper.getLast(userId);
			}
			if (userKyc != null) {
				KycFlowContext.getContext().setUserKyc(userKyc);
			}
		}

		if (KycCertificateKycType.COMPANY.equals(kycType)) {
			CompanyCertificate companyCertificate = KycFlowContext.getContext().getCompanyCertificate();
			if (companyCertificate == null) {
				companyCertificate = this.companyCertificateMapper.getLast(userId);
			}
			if (companyCertificate != null) {
				KycFlowContext.getContext().setCompanyCertificate(companyCertificate);
			}
		}

		// 尝试初始化jumio信息
		try {
			boolean isSdk = Objects.equals(TerminalEnum.ANDROID, kycFlowRequest.getSource())
					|| Objects.equals(TerminalEnum.IOS, kycFlowRequest.getSource());
			KycFlowResponse flowResponse;
			if (isSdk) {
				flowResponse = initSdkJumio(userId, kycType);
				kycCertificate.setFaceStatus(KycCertificateStatus.SKIP.name());
			} else {
				// web 端jumio 流程是否需要做人脸识别
				flowResponse = initWebJumio(userId, kycType);
				if (!config.isKycJumioFaceSwitch()) {
					kycCertificate.setFaceStatus(KycCertificateStatus.SKIP.name());
				}else {
					kycCertificate.setFaceStatus(null);
				}
			}
			// 变更jumio 状态到处理中
			kycCertificate.setJumioStatus(KycCertificateStatus.PROCESS.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateJumioStatusWithFace(kycCertificate);

			return flowResponse;

		} catch (BusinessException e) {
			log.warn("KYC认证JUMIO => 初始化失败. userId:{}", userId, e);
			// 需要判断错误的原因，如果是已经通过，则直接变更到通过，如果是review 直接变更到review, 其他到错误不管，直接抛出上层
			if (Objects.equals(InspectorErrorCode.JUMIO_INIT_PASSED_ERROR.getCode(), e.getBizCode())) {
				// JUMIO 认证已经通过.
				kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
				kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
				kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
				kycCertificateMapper.updateJumioStatus(kycCertificate);
				log.info("KYC认证JUMIO => jumio认证已经通过，不能再做. userId:{}", userId);
				// todo 这两种情况下，是否可以直接进入下一步人脸识别认证上
//				KycFlowContext.getContext().setKycFlowType(KycFlowType.SKIP_JUMIO);
				throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
			} else if (Objects.equals(InspectorErrorCode.JUMIO_INIT_REVIEW_ERROR.getCode(), e.getBizCode())) {
				// JUMIO 认证正在审核中
				kycCertificate.setJumioStatus(KycCertificateStatus.REVIEW.name());
				kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
				kycCertificateMapper.updateJumioStatus(kycCertificate);
				log.info("KYC认证JUMIO => jumio认证正在审核中. userId:{}", userId);
				// todo 这两种情况下，是否可以直接进入下一步人脸识别认证上
//				KycFlowContext.getContext().setKycFlowType(KycFlowType.SKIP_JUMIO);
				throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
			} else {
				throw e;
			}
		}
	}

	private KycFlowResponse initWebJumio(UserKyc userKyc) {
		JumioInitResponse response = new JumioInitResponse();

		Long userId = userKyc.getUserId();

		if (StringUtils.isNotBlank(userKyc.getJumioId())) {
			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, userKyc.getJumioId());
			if (jumio != null && !StringUtils.equalsIgnoreCase(jumio.getSource(), JumioScanSource.SDK.name())
					&& StringUtils.isNotBlank(jumio.getAuthToken())) {
				response.setRedirectUrl(jumio.getAuthToken());
				return response;
			}
		}

		String kycId = String.valueOf(userKyc.getId());

		Jumio jumio = jumioBusiness.initWebJumio(userId, com.binance.inspector.common.enums.JumioHandlerType.USER_KYC,
				kycId, false);
		log.info("初始化 JUMIO WEB 端jumio 数据成功, userId:{} kycId:{} jumioId:{}", userId, kycId, jumio.getId());
		// 如果正常没有抛错退出，则在user_kyc中保存对应的jumio 关联信息
		userKyc.setJumioId(jumio.getId());
		userKyc.setScanReference(jumio.getScanReference());
		userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.saveJumioId(userKyc);
		response.setRedirectUrl(jumio.getAuthToken());
		return response;
	}

	private KycFlowResponse initSdkJumio(UserKyc userKyc) {
		JumioInitResponse response = new JumioInitResponse();

		if (StringUtils.isNotBlank(userKyc.getJumioId())) {
			// 看下是否存在有老的JUMIO初始化的数据，如果有，且是web端，则不能让他再用SDK申请
			Jumio jumio = jumioMapper.selectByPrimaryKey(userKyc.getUserId(), userKyc.getJumioId());
			if (jumio != null) {
				log.info("存在有上一笔初始化的JUMIO数据，直接进行删除重建: userId:{} kycId:{} jumioId:{}", userKyc.getUserId(),
						userKyc.getId(), jumio.getId());
				jumioMapper.deleteByPrimaryKey(userKyc.getUserId(), jumio.getId());
				userKyc.setJumioId(null);
				userKyc.setScanReference(null);
			}
		}

		JumioSdkInitResponse initResponse = jumioBusiness.initSdkJumio(userKyc.getUserId(),
				com.binance.inspector.common.enums.JumioHandlerType.USER_KYC, String.valueOf(userKyc.getId()), false);
		// 如果没有抛出错误退出, 则在user_kyc中保存对应的jumio 关联信息
		userKyc.setJumioId(initResponse.getJumioId());
		// 先与填写自定义的流水号，在上传完成后需要变更到JUMIO的流水号
		userKyc.setScanReference(initResponse.getMerchantReference());
		userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.saveJumioId(userKyc);
		response.setApiKey(initResponse.getApiKey());
		response.setApiSecret(initResponse.getApiSecret());
		response.setMerchantReference(initResponse.getMerchantReference());
		response.setUserReference(initResponse.getUserReference());
		response.setCallBack(initResponse.getCallBack());

		return response;
	}

	private KycFlowResponse initWebJumio(Long userId, KycCertificateKycType kycType) {
		JumioInitResponse response = new JumioInitResponse();
		response.setUserId(userId);
		response.setKycType(kycType);
		final String flowId = UUIDUtils.getId();
		JumioHandlerType handlerType = KycCertificateKycType.COMPANY != kycType ? JumioHandlerType.USER_KYC
				: JumioHandlerType.COMPANY_KYC;
		InitJumioResponse jumioResponse = jumioBusiness.initWebJumioWithoutSave(userId, handlerType, flowId, true);
		String jumioRedirectUrl = jumioResponse == null ? null : jumioResponse.getRedirectUrl();
		if (StringUtils.isBlank(jumioRedirectUrl)) {
			log.error("KYC认证JUMIO => init jumio web url fail. userId: userId:{} kycType:{} flowId", userId, kycType,
					flowId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		response.setRedirectUrl(jumioRedirectUrl);

		UserKyc userKyc = KycFlowContext.getContext().getUserKyc();
		CompanyCertificate companyCertificate = KycFlowContext.getContext().getCompanyCertificate();
		if (doubleWrite()) {
			Jumio jumio = new Jumio();
			jumio.setUserId(userId);
			jumio.setType(JumioType.getByName(handlerType.getCode()));
			jumio.setScanReference(jumioResponse.getTransactionReference());
			jumio.setAuthToken(jumioResponse.getRedirectUrl());
			jumio.setCreateTime(DateUtils.getNewUTCDate());
			jumio.setUpdateTime(DateUtils.getNewUTCDate());
			jumioMapper.insert(jumio);

			if (userKyc != null) {
				userKyc.setJumioId(jumio.getId());
				userKyc.setScanReference(jumio.getScanReference());
				userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
				userKyc.setUpdateTime(DateUtils.getNewUTCDate());
				userKycMapper.saveJumioId(userKyc);
			}

			if (companyCertificate != null) {
				companyCertificate.setJumioId(jumio.getId());
				companyCertificate.setScanReference(jumio.getScanReference());
				companyCertificate.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
				companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
				companyCertificateMapper.saveJumioId(companyCertificate);
			}
		}

		return response;
	}

	private KycFlowResponse initSdkJumio(Long userId, KycCertificateKycType kycType) {
		JumioInitResponse response = new JumioInitResponse();
		response.setUserId(userId);
		response.setKycType(kycType);
		final String flowId = UUIDUtils.getId();
		JumioHandlerType handlerType = KycCertificateKycType.COMPANY != kycType ? JumioHandlerType.USER_KYC
				: JumioHandlerType.COMPANY_KYC;
		InitSdkJumioResponse jumioResponse = jumioBusiness.initSdkJumioWithoutSave(userId, handlerType, flowId, true);
		if (jumioResponse == null) {
			log.error("KYC认证JUMIO => init jumio web url fail. userId: userId:{} kycType:{} flowId", userId, kycType,
					flowId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		response.setApiKey(jumioResponse.getApiKey());
		response.setApiSecret(jumioResponse.getApiSecret());
		response.setMerchantReference(jumioResponse.getMerchantReference());
		response.setUserReference(jumioResponse.getUserReference());
		response.setCallBack(jumioResponse.getCallBack());

		UserKyc userKyc = KycFlowContext.getContext().getUserKyc();
		if (doubleWrite() && userKyc != null) {
			Jumio jumio = new Jumio();
			jumio.setUserId(userId);
			jumio.setType(JumioType.getByName(handlerType.getCode()));
			jumio.setSource(JumioScanSource.SDK.name());
			jumio.setMerchantReference(response.getMerchantReference());
			jumio.setCreateTime(DateUtils.getNewUTCDate());
			jumio.setUpdateTime(DateUtils.getNewUTCDate());
			jumioMapper.insert(jumio);

			userKyc.setJumioId(jumio.getId());
			// 先与填写自定义的流水号，在上传完成后需要变更到JUMIO的流水号
			userKyc.setScanReference(jumio.getMerchantReference());
			userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
			userKyc.setUpdateTime(DateUtils.getNewUTCDate());
			userKycMapper.saveJumioId(userKyc);
		}

		return response;
	}
}
