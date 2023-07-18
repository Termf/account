package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.mq.IdmNotifyMsgSender;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.vo.country.CountryVo;
import com.binance.fiatpayment.core.api.FiatPrimeTrustSpecialApi;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.request.BaseInfoKycIdmRequest;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Log4j2
@Service
public class UsL2ToL1EndHandler extends AbstractEndHandler {
	@Resource
	private IdmApi idmApi;
	@Resource
	private FiatPrimeTrustSpecialApi fiatPrimeTrustSpecialApi;
	
	@Resource
	private IdmNotifyMsgSender idmNotifyMsgSender;

	@Override
	public boolean isDoHandler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		Integer level = kycCertificate.getKycLevel();
		if (!Objects.equals(KycCertificateKycLevel.L2.getCode(), level)) {
			return false;
		}
		if (!StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getJumioStatus())) {
			return true;
		}
		// face 不通过 && face 不为跳过 出发L2 -> L1
		if (!StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getFaceStatus())
				&& !StringUtils.equalsIgnoreCase(KycCertificateStatus.SKIP.name(), kycCertificate.getFaceStatus())) {
			return true;
		}

		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		boolean isDo = false;
		switch (kycType) {
		case USER:
			isDo = isUserDoHandler(kycCertificate);
			break;
		case COMPANY:
			isDo = isCompanyDoHandler(kycCertificate);
			break;
		default:
			break;
		}

		return isDo;

	}

	/**
	 * 判断用户
	 * 
	 * @param kycCertificate
	 * @return
	 */
	private boolean isUserDoHandler(KycCertificate kycCertificate) {
		if (!StringUtils.equalsAnyIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getAddressStatus())) {
			return true;
		}
		if (StringUtils.isBlank(kycCertificate.getBindMobile())) {
			return true;
		}
		return false;
	}

	/**
	 * 判断企业
	 * 
	 * @param kycCertificate
	 * @return
	 */
	private boolean isCompanyDoHandler(KycCertificate kycCertificate) {
		if (!StringUtils.equalsAnyIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getGoogleFormStatus())) {
			return true;
		}
		return false;
	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void handler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		log.info("US KYC L2 to L1 => L2 to L1. userId:{}", kycCertificate.getUserId());
		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		JumioInfoVo jumioInfoVo = getLastJumio(kycCertificate.getUserId());
		if (jumioInfoVo == null) {
			log.warn("US KYC L2 to L1 => jumio数据获取识别. userId:{}", kycCertificate.getUserId());
			return;
		}
		Long userId = kycCertificate.getUserId();
		CountryVo country = iCountry.getCountryByAlpha3(jumioInfoVo.getIssuingCountry());
		if (country == null) {
			log.warn("US KYC L2 to L1 => jumio国籍码获取国家信息失败. userId:{} issuingCountry:{}", userId,
					jumioInfoVo.getIssuingCountry());
			return;
		}
		/*
		 * 1. 把kyc 等级由 L2 变更到 L1 2. 把安全等级变更到 2 3. 把用户状态变更到未认证 4. 清除绑定的证件关系 5.
		 * 如果是jumio认证拒绝的情况下，把人脸识别状态和人脸识别的正式照片清除 6. 发送通知邮件 ps:企业会员，直接从 L2->L0
		 */
		KycCertificateKycLevel targetLevel = KycCertificateKycType.COMPANY.equals(kycType) ? KycCertificateKycLevel.L0
				: KycCertificateKycLevel.L1;
		kycCertificate.setKycLevel(targetLevel.getCode());

		updateKycFlowStatus(kycCertificate.getUserId(), targetLevel.getCode(), KycCertificateStatus.PROCESS,
				kycCertificate.getMessageTips());
		int securityLevel = updateSecurityLevel(userId, 2);
		log.info("US KYC L2 to L1 => security level change. userId:{} row:{}", userId, securityLevel);
		int userStatus = iUserCertificate.updateCertificateStatus(userId, false);
		log.info("US KYC L2 to L1 => user status of kyc change. userId:{} row:{}", userId, userStatus);
		int idNumberIndex = removeIdNumberMapIndex(jumioInfoVo, country);
		log.info("US KYC L2 to L1 => id number index remove. userId:{} row:{} jumioId:{}", userId, idNumberIndex,
				jumioInfoVo.getId());
		if (!StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getJumioStatus())
				&& StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getFaceStatus())) {
			log.info("US KYC L2 to L1 => jumio 不是通过的状态且人脸识别处于通过状态,需要把人脸识别重置. userId:{} jumioStatus:{}", userId,
					jumioInfoVo.getStatus());
			kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setFaceTips(kycCertificate.getJumioTips());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceStatus(kycCertificate);
			// 删除人脸识别的正式照片
			iFace.removeFaceReferenceRefImage(userId);
		}

		String messageTips = "";
		switch (KycCertificateKycType.getByCode(kycCertificate.getKycType())) {
		case USER:
			if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getAddressStatus())) {
				messageTips = kycCertificate.getAddressTips();
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
				messageTips = kycCertificate.getJumioTips();
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
				messageTips = kycCertificate.getFaceTips();
			}
			break;
		case COMPANY:
			if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getBaseFillStatus())) {
				messageTips = kycCertificate.getBaseFillTips();
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getGoogleFormStatus())) {
				messageTips = kycCertificate.getGoogleFormTips();
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
				messageTips = kycCertificate.getJumioTips();
			} else if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
				messageTips = kycCertificate.getFaceTips();
			}
			break;
		default:
			break;
		}

		// send email
		sendLevelChangeEmail(userId, jumioInfoVo.getBaseLanguage(), messageTips, UserConst.US_KYC_EMAIL_L2_TO_L1,
				"KYC认证L2通过邮件");

		// 通知fiat_payment resetPtStatus
		try {
			fiatPrimeTrustSpecialApi.resetPtStatus(APIRequest.instance(userId.toString()));
		}catch (Exception e) {
			log.error("US KYC L2 to L1 => 通知 fiatPayment reset PT status fail. userId:{}", userId, e);
		}

		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		// 个人用户需要上送idm reject 信息
		if (baseInfo != null && KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
			request.setUserId(userId);
			request.setTid(baseInfo.getIdmTid());
			String reason;
			if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getAddressStatus())) {
				reason = MessageMapHelper.getMessage(kycCertificate.getAddressTips(), LanguageEnum.EN_US);
			}else if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
				reason = MessageMapHelper.getMessage(kycCertificate.getJumioTips(), LanguageEnum.EN_US);
			}else if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
				reason = MessageMapHelper.getMessage(kycCertificate.getFaceTips(), LanguageEnum.EN_US);
			}else {
				reason = "Rejected";
			}
			request.setReason(reason);
			try {
				log.info("US KYC L2 to L1 => 上报IDM rejected信息. userId:{},request:{}", userId, request);
				idmApi.idmReject(APIRequest.instance(request));
			}catch(Exception e) {
				log.error("US KYC L2 to L1 => 上报IDM rejected信息. userId:{},request:{}", userId, request, e);
				idmNotifyMsgSender.notifyIdmReject(request);
			}
		}

	}
}
