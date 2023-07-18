package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.master.enums.LanguageEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Log4j2
@Service
public class UsKycL1EmailEndHandle extends AbstractEndHandler {
	
	@Override
	public boolean isDoHandler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();

		// base 不为pass不执行
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			return false;
		}
		// 非Level 1 不触发
		if (!KycCertificateKycLevel.L1.getCode().equals(kycCertificate.getKycLevel())) {
			return false;
		}
		
		// address，jumio，face 未到终态 不执行。
		if (!isAllEndStatus(kycCertificate.getAddressStatus(), kycCertificate.getJumioStatus(),
				kycCertificate.getFaceStatus())) {
			return false;
		}

		// address，jumio，face 任何一个是refused 执行
		if (StringUtils.equalsAny(KycCertificateStatus.REFUSED.name(), kycCertificate.getAddressStatus(),
				kycCertificate.getJumioStatus(), kycCertificate.getFaceStatus())) {
			return true;
		}

		return false;
	}

	@Override
	public void handler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		StringBuffer reason = new StringBuffer();
		Set<String> reasonSet = new HashSet<>();
		log.info("发送kyc拒绝邮件 userId:{}", kycCertificate.getUserId());
		if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getAddressStatus())) {
			if(!reasonSet.contains(kycCertificate.getAddressTips())) {
				reason.append(MessageMapHelper.getMessage(kycCertificate.getAddressTips(), LanguageEnum.EN_US)).append(" ");
				reasonSet.add(kycCertificate.getAddressTips());
			}
		}

		if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
			if(!reasonSet.contains(kycCertificate.getJumioTips())) {
				reason.append(MessageMapHelper.getMessage(kycCertificate.getJumioTips(), LanguageEnum.EN_US)).append(" ");
				reasonSet.add(kycCertificate.getJumioTips());
			}
		}

		if (KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
			if(!reasonSet.contains(kycCertificate.getFaceTips())) {
				reason.append(MessageMapHelper.getMessage(kycCertificate.getFaceTips(), LanguageEnum.EN_US)).append(" ");
				reasonSet.add(kycCertificate.getFaceTips());
			}
		}

		if (reason.length() < 1) {
			return;
		}
		sendLevelChangeEmail(kycCertificate.getUserId(), LanguageEnum.EN_US, reason.toString(), UserConst.US_KYC_L1_END,
				"KYC Leve1执行完后存在REFUSED状态");

	}

	/**
	 * REFUSED,SKIP,PASS则为终态
	 * 
	 * @param s
	 * @return
	 */
	private boolean isAllEndStatus(String... s) {
		for (String t : s) {
			if (StringUtils.isBlank(t) || KycCertificateStatus.PROCESS.name().equals(t)
					|| KycCertificateStatus.REVIEW.name().equals(t)) {
				return false;
			}
		}
		return true;
	}

}
