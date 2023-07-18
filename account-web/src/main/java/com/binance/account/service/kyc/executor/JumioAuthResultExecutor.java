package com.binance.account.service.kyc.executor;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.mq.KycJumioInfoMsgExecutor;
import com.binance.account.service.certificate.IKycExceptionTask;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFLowExecutorHelper;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.kyc.request.JumioAuthRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.common.model.KycCertificateVo;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import com.binance.messaging.common.utils.UUIDUtils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Log4j2
@Service
public class JumioAuthResultExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	private IUserFace iUserFace;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private KycFLowExecutorHelper kycFLowExecutorHelper;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private JumioMapper jumioMapper;
	@Resource
	private CountryMapper countryMapper;
	@Resource
	private KycJumioInfoMsgExecutor kycJumioInfoMsgExecutor;
	@Resource
	private IKycExceptionTask iKycExceptionTask;

	@Resource
	private KycCertificateApi certificateApi;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		KycFlowResponse response = new KycFlowResponse();
		if (kycFlowRequest == null) {
			return response;
		}
		Long userId = kycFlowRequest.getUserId();
		response.setKycType(kycFlowRequest.getKycType());
		response.setUserId(userId);
		JumioAuthRequest authRequest = (JumioAuthRequest) kycFlowRequest;
		// 获取kyc认证信息
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		if (kycCertificate == null) {
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}
		KycCertificateStatus oldJumoStatus = KycCertificateStatus.getByName(kycCertificate.getJumioStatus());
		// jumio 状态转化
		JumioStatus jumioStatus = JumioStatus.getByName(authRequest.getJumioStatus());
		if (jumioStatus == null) {
			log.warn("KYC JUMIO AUTH => 状态转换错误. userId:{} authStatus:{}", userId, authRequest.getJumioStatus());
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 只处理 review/pass/refused等状态
		kycCertificate.setJumioTips(authRequest.getMessage());
		switch (jumioStatus) {
		case PASSED:
			kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
			break;
		case REVIEW:
		case UPLOADED:
			kycCertificate.setJumioStatus(KycCertificateStatus.REVIEW.name());
			break;
		case REFUED:
		case ERROR:
		case EXPIRED:
			kycCertificate.setJumioStatus(KycCertificateStatus.REFUSED.name());
			if (StringUtils.isNotBlank(kycCertificate.getGoogleFormStatus())) {
				kycCertificate.setGoogleFormStatus(KycCertificateStatus.REFUSED.name());
			}
			kycCertificate.setBaseFillStatus(KycCertificateStatus.REFUSED.name());
			// 发送basic mq
			certificateToSendKycChangeMsg(kycCertificate);
			break;
		default:
			// do nothing
			return response;
		}
		log.info("KYC JUMIO AUTH => JUMIO状态变更: userId:{}, jumioStatus:{}, message:{}", userId, jumioStatus,
				authRequest.getMessage());
		kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		kycCertificateMapper.updateJumioAndBaseStatus(kycCertificate);
		// 设置上下文信息
		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFlowResponse(response);

		// jumio pass 情况下，检查当前流程是否已经发起过人脸识别，如果未有，进行发起人脸识别, SDK 的jumio 不要需要人脸识别
		if (StringUtils.equalsAny(kycCertificate.getJumioStatus(), KycCertificateStatus.PASS.name(),
				KycCertificateStatus.REVIEW.name())
				&& !KycCertificateStatus.SKIP.name().equals(kycCertificate.getFaceStatus())) {
			if (StringUtils.isNotBlank(authRequest.getBizId())) {
				KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
				FaceTransType faceTransType = kycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
						: FaceTransType.KYC_COMPANY;

				JumioInfoVo jumioInfoVo = null;

				if (authRequest.getJumioInfoVo() != null) {
					jumioInfoVo = JSON.parseObject(authRequest.getJumioInfoVo().toString(), JumioInfoVo.class);
				}

				boolean jumioIsKyc = jumioInfoVo == null ? false
						: JumioHandlerType.COMPANY_KYC.equals(jumioInfoVo.getHandlerType())
								|| JumioHandlerType.USER_KYC.equals(jumioInfoVo.getHandlerType());

				TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(authRequest.getBizId(),
						faceTransType.name());
				if (faceLog == null) {
					String bizId = jumioIsKyc ? authRequest.getBizId() : UUIDUtils.getId();
					log.info("KYC JUMIO结果->KYC认证流程还未发起人脸识别，进行发起人脸识别认证流程. userId:{} transId:{} transType:{}", userId,
							authRequest.getBizId(), faceTransType);
					try {
						iUserFace.initFaceFlowByTransId(bizId, userId, faceTransType, true, true);
					} catch (Exception e) {
						log.warn(
								"KYC JUMIO结果->KYC认证流程还未发起人脸识别，进行发起人脸识别认证流程失败，创建异常补偿任务. userId:{} transId:{} transType:{}",
								userId, bizId, faceTransType);
						iKycExceptionTask.addJumioInitFaceException(userId, faceTransType, bizId);
					}
					kycCertificate.setFaceStatus(KycCertificateStatus.PROCESS.name());
					kycCertificate.setFaceTips(null);
					kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
					kycCertificateMapper.updateFaceStatus(kycCertificate);
				}
			}
		}

		// Jumio 审核变化的话，需要处理一些后续处理逻辑(这些后续处理是由于可能不会触发End处理器的补充)
		kycFLowExecutorHelper.jumioChangeAfterHandler(kycCertificate, oldJumoStatus);

		doDoubleWrite(kycCertificate, authRequest);

		return response;
	}

	private void doDoubleWrite(KycCertificate kycCertificate, JumioAuthRequest authRequest) {
		if (!doubleWrite()) {
			return;
		}
		if (authRequest.getJumioInfoVo() == null) {
			return;
		}

		Long userId = kycCertificate.getUserId();
		JumioInfoVo jumioInfoVo = JSON.parseObject(authRequest.getJumioInfoVo().toString(), JumioInfoVo.class);
		JumioHandlerType handlerType = jumioInfoVo.getHandlerType();

		if (!JumioHandlerType.USER_KYC.equals(handlerType) && !JumioHandlerType.COMPANY_KYC.equals(handlerType)) {
			return;
		}
		String bizId = "";
		String jumioId = null;
		UserKyc userKyc = null;
		String scanRef = "";
		CompanyCertificate companyCertificate = null;
		if (JumioHandlerType.USER_KYC.equals(handlerType)) {
			userKyc = userKycMapper.getLast(userId);
			bizId = userKyc.getId() + "";
			jumioId = userKyc.getJumioId();
			scanRef = userKyc.getScanReference();
		} else {
			companyCertificate = companyCertificateMapper.getLast(userId);
			bizId = companyCertificate.getId() + "";
			jumioId = companyCertificate.getJumioId();
			scanRef = companyCertificate.getScanReference();
		}

		if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
			// JUMIO 的状态不是最终态时，先不做处理
			log.info("JUMIO结果处理还未到最终态, userId:{} bizId:{} type:{}", kycCertificate.getUserId(), bizId,
					jumioInfoVo.getHandlerType());
			return;
		}

		if (StringUtils.isBlank(jumioId) || !StringUtils.equalsAnyIgnoreCase(scanRef, jumioInfoVo.getScanReference(),
				jumioInfoVo.getMerchantReference())) {
			log.info("KYC JUMIO结果->根据业务编号和用户信息，获取对应的jumio_id 失败. userId:{} bizId:{}, type:{} scanRef:{}", userId, bizId,
					handlerType, scanRef);
			return;
		}

		Jumio jumio = jumioMapper.selectByPrimaryKey(userId, jumioId);
		if (jumio == null || jumio.getStatus() != null) {
			log.info("KYC JUMIO结果->根据 jumioId 获取对应的 JUMIO 信息失败或者状态已经不在未处理状态. userId:{} bizId:{} jumioId:{}", userId,
					bizId, jumioId);
			return;
		}

		// 下面条件只要有一个相同，就认为是同一笔数据
		if (!(StringUtils.equalsIgnoreCase(jumio.getScanReference(), jumioInfoVo.getScanReference())
				|| StringUtils.equalsIgnoreCase(jumio.getMerchantReference(), jumioInfoVo.getMerchantReference()))) {
			log.info("KYC JUMIO结果->JUMIO的唯一标识不一致，不进行任何处理. userId:{} bizId:{} scanRef:{} infoScanRef:{}", userId, bizId,
					jumio.getScanReference(), jumioInfoVo.getScanReference());
			return;
		}

		Country country = null;
		if (StringUtils.isNotBlank(jumioInfoVo.getIssuingCountry())) {
			country = countryMapper.selectByCode2(jumioInfoVo.getIssuingCountry());
		}
		if (country == null) {
			log.error("KYC JUMIO结果->获取的国家编码不存在，无法保存用户证件信息: userId:{} bizId:{} issuingCountry:{}", userId, bizId,
					jumioInfoVo.getIssuingCountry());
			return;
		}
		kycJumioInfoMsgExecutor.setJumioByMessageInfo(jumio, jumioInfoVo, country);
		kycJumioInfoMsgExecutor.setJumioStatus(userId, bizId, jumio, jumioInfoVo);
		jumioMapper.updateByPrimaryKeySelective(jumio);

		boolean jumioPass = com.binance.account.common.enums.JumioStatus.jumioPassed.equals(jumio.getStatus());

		if (userKyc != null) {
			if (jumioPass) {
				userKyc.setStatus(KycStatus.jumioPassed);
			} else {
				// jumio 拒绝时，业务直接进入拒绝状态
				userKyc.setStatus(KycStatus.refused);
			}

			UserKyc record = new UserKyc();
			record.setId(userKyc.getId());
			record.setUserId(userKyc.getUserId());
			record.setStatus(userKyc.getStatus());
			record.setFailReason(jumioPass ? null : jumioInfoVo.getFailReason());
			if (jumioPass) {
				record.setCheckStatus(JumioStatus.PASSED.name());
			} else {
				record.setCheckStatus(JumioStatus.REFUED.name());
			}
			record.setUpdateTime(DateUtils.getNewUTCDate());
			userKycMapper.updateStatus(record);

		}

		if (companyCertificate != null) {
			if (CompanyCertificateStatus.isEndStatus(companyCertificate.getStatus())) {
				log.info("当前企业认证流程已经进入终态，不能再变更. userId:{} jumioId:{}", jumio.getUserId(), jumio.getId());
				return;
			}

			if (jumioPass) {
				companyCertificate.setStatus(CompanyCertificateStatus.jumioPassed);
			} else {
				companyCertificate.setStatus(CompanyCertificateStatus.refused);
			}
			CompanyCertificate record = new CompanyCertificate();
			record.setId(companyCertificate.getId());
			record.setUserId(companyCertificate.getUserId());
			record.setStatus(companyCertificate.getStatus());
			record.setInfo(jumioPass ? null : jumioInfoVo.getFailReason());
			if (jumio.getStatus() == com.binance.account.common.enums.JumioStatus.jumioPassed) {
				record.setJumioStatus(JumioStatus.PASSED.name());
			} else {
				record.setJumioStatus(JumioStatus.REFUED.name());
			}
			record.setUpdateTime(DateUtils.getNewUTCDate());
			companyCertificateMapper.updateByPrimaryKeySelective(record);
		}

	}

	private void certificateToSendKycChangeMsg(KycCertificate kycCertificate) {
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			return;
		}
		try {
			KycCertificateVo certificate = new KycCertificateVo();
			BeanUtils.copyProperties(kycCertificate, certificate);
			certificate.setKycType(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())
					? com.binance.certification.common.enums.KycCertificateKycType.COMPANY
					: com.binance.certification.common.enums.KycCertificateKycType.USER);
			certificateApi.sendBasicChangeMq(APIRequest.instance(certificate));
		} catch (Exception e) {
			log.warn("调用certificate发送kyc mq异常 userId:{}", kycCertificate.getUserId(), e);
		}
	}
}
