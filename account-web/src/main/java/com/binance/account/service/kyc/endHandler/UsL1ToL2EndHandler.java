package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.mq.IdmNotifyMsgSender;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.vo.country.CountryVo;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.vo.idm.request.BaseInfoKycIdmRequest;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.annotations.DefaultDB;
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
public class UsL1ToL2EndHandler extends AbstractEndHandler {
	@Resource
	private IdmApi idmApi;
	
	@Resource
	private IdmNotifyMsgSender idmNotifyMsgSender;

	@Override
	public boolean isDoHandler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		// step 1: 必须是L1, 地址认证, kycMobile 绑定, jumio, face 都通过
		// PS company level 直接从 L0->L2
		Integer currLevel = kycCertificate.getKycLevel();
		if (!Objects.equals(KycCertificateKycLevel.L1.getCode(), currLevel)
				&& KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			// 当前等级不是L1
			return false;
		}
		if (!Objects.equals(KycCertificateKycLevel.L0.getCode(), currLevel)
				&& KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			// 当前等级不是L1
			return false;
		}

		if (!StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getJumioStatus())) {
			return false;
		}
		// 人脸不通过 && 人脸不跳过
		if (!StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getFaceStatus())
				&& !StringUtils.equalsIgnoreCase(KycCertificateStatus.SKIP.name(), kycCertificate.getFaceStatus())) {
			return false;
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
			return false;
		}
		if (StringUtils.isBlank(kycCertificate.getBindMobile())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断企业
	 * 
	 * @param kycCertificate
	 * @return
	 */
	private boolean isCompanyDoHandler(KycCertificate kycCertificate) {
		if (!StringUtils.equalsAnyIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getGoogleFormStatus())) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void handler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		log.info("US KYC L1 to L2 => 开始处理. userId:{}", kycCertificate.getUserId());
		Long userId = kycCertificate.getUserId();
		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		JumioInfoVo jumioInfoVo = getLastJumio(kycCertificate.getUserId());
		if (jumioInfoVo == null) {
			log.warn("US KYC L1 to L2 => jumio data get fail. userId:{}", userId);
			return;
		}
		CountryVo country = iCountry.getCountryByAlpha3(jumioInfoVo.getIssuingCountry());
		if (country == null) {
			log.warn("US KYC L1 to L2 => jumio国籍码获取国家信息失败. userId:{} issuingCountry:{}", userId,
					jumioInfoVo.getIssuingCountry());
			return;
		}
		// 1. 检查是否满足证件号锁定条件
		if (super.isIdNumberUsedByOther(jumioInfoVo, country)) {
			/*
			 * 证件被占用 1.1 jumio_status 变更到 REFUSED 通知inspector 变更jumio的状态到 REFUSED. 1.2
			 * face_status 变更到 REFUSED 同步
			 */
			log.info("US KYC L1 to L2 => 由于证件号被占用进行拒绝JUMIO. userId:{}", userId);
			kycCertificate.setJumioStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setJumioTips(JumioError.ID_NUMBER_USED.name());
			if (!KycCertificateStatus.SKIP.name().equalsIgnoreCase(kycCertificate.getFaceStatus())) {
				kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
				kycCertificate.setFaceTips(JumioError.ID_NUMBER_USED.name());
			}
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateJumioStatus(kycCertificate);
			kycCertificateMapper.updateFaceStatus(kycCertificate);
			super.refusedJumioByNumberUsed(jumioInfoVo);
			// 把人脸识别也进行拒绝且清除人脸识别到关联照片
			iFace.removeFaceReferenceRefImage(userId);
			return;
		}
		/*
		 * 2. 如果校验证件号通过对情况下，对用户等级做出变化 2.1 变更用户KYC等级到L2 2.2 变更用户对安全等级到3级 2.3 变更用户状态和设置母账号
		 * 2.4 存储用户的证件映射表信息 2.5 把用户的人脸识别照片由检验照片存储为正式验证照片 2.6 发送通知邮件 2.7 触发提币人脸识别的业务逻辑检查
		 */
		kycCertificate.setKycLevel(KycCertificateKycLevel.L2.getCode());
		updateKycFlowStatus(kycCertificate.getUserId(), KycCertificateKycLevel.L2.getCode(), KycCertificateStatus.PASS,
				kycCertificate.getMessageTips());
		int securityLevelRow = updateSecurityLevel(userId, 3);
		log.info("US KYC L1 to L2 => 安全等级变更, userId:{} row:{}", userId, securityLevelRow);
		int userStatus = iUserCertificate.updateCertificateStatus(userId, true);
		log.info("US KYC L1 to L2 => 用户状态和母账号标识. userId:{} row:{}", userId, userStatus);
		super.saveIdNumberMapIndex(kycType, jumioInfoVo, country);
		log.info("US KYC L1 to L2 => 保存用户证件号映射表信息. userId:{}", userId);
		boolean faceRefImage = iFace.saveFaceReferenceRefImage(userId);
		log.info("US KYC L1 to L2 => 用户人脸识别图片由检查照片变更到正式照. userId:{} faceRefImage:{}", userId, faceRefImage);
		super.sendLevelChangeEmail(userId, jumioInfoVo.getBaseLanguage(), kycCertificate.getMessageTips(),
				UserConst.US_KYC_EMAIL_L1_TO_L2, "KYC认证L2通过邮件");

		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		//个人用户需要上送idm
		if (baseInfo != null && KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			BaseInfoKycIdmRequest request = new BaseInfoKycIdmRequest();
			request.setUserId(userId);
			request.setTid(baseInfo.getIdmTid());
			try {
				log.info("US KYC L1 to L2 => 上报IDM accpet信息,添加Tier_2的TAG. userId:{},request:{}", userId, request);
				idmApi.idmAccept(APIRequest.instance(request));
			}catch(Exception e) {
				log.error("US KYC L1 to L2 => 上报IDM accpet信息,添加Tier_2的TAG失败转MQ通知. userId:{},request:{}", userId, request,e);
				idmNotifyMsgSender.notifyIdmAccept(request);
			}
		}

	}
}
