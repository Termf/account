package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.KycTaxidIndex;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;

import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.report.vo.user.UsUserSet0CommissionRequest;
import lombok.extern.log4j.Log4j2;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class UsL0ToL1EndHandler extends AbstractEndHandler {

	@Override
	public boolean isDoHandler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		// 企业认证 提交 base info 等级不变。
		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			return false;
		}
		// 当前等级是L0 且 base-fill pass
		Integer kycLevel = kycCertificate.getKycLevel();
		KycCertificateStatus baseStatus = KycCertificateStatus.getByName(kycCertificate.getBaseFillStatus());
		return kycLevel == null || kycLevel < KycCertificateKycLevel.L1.getCode()
				&& Objects.equals(KycCertificateStatus.PASS, baseStatus);
	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void handler() {
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		log.info("US KYC认证后置处理 => L0 to L1. userId:{}", kycCertificate.getUserId());
		// 1. 等级 LO 变更到L1
		updateKycFlowStatus(kycCertificate.getUserId(), KycCertificateKycLevel.L1.getCode(),
				KycCertificateStatus.PROCESS, kycCertificate.getMessageTips());
		kycCertificate.setKycLevel(KycCertificateKycLevel.L1.getCode());
		// 个人用户维护 kyc_taxid_index
		if (KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			KycFillInfo kycFillInfo = getMasterKycFillInfo(kycCertificate.getUserId(), KycFillType.BASE);
			KycTaxidIndex kycTaxidIndex = new KycTaxidIndex();
			kycTaxidIndex.setTaxId(kycFillInfo.getTaxId());
			kycTaxidIndex.setUserId(kycCertificate.getUserId());
			try {
				kycTaxidIndexMapper.insert(kycTaxidIndex);
			} catch (DuplicateKeyException e) {
				kycTaxidIndex = kycTaxidIndexMapper.selectByPrimaryKey(kycFillInfo.getTaxId());
				if(!kycTaxidIndex.getUserId().equals(kycCertificate.getUserId())) {
					throw new BusinessException(AccountErrorCode.KYC_TAXID_IS_USED);
				}
			}
		}

		// 2. user_security level 变更为 2
		updateSecurityLevel(kycCertificate.getUserId(), 2);
		// send notify email
		sendLevelChangeEmail(kycCertificate.getUserId(), LanguageEnum.EN_US, kycCertificate.getMessageTips(),
				UserConst.US_KYC_EMAIL_L0_TO_L1, "KYC认证L1通过邮件");

		//美国站过了L1默认免手续费一个月
        if(config.isUsEnableTradeFeeZero()){
			UsUserSet0CommissionRequest usUserSet0CommissionRequest = new UsUserSet0CommissionRequest();
			List<Long> users = new ArrayList<>();
			users.add(kycCertificate.getUserId());
			usUserSet0CommissionRequest.setUserIds(users);
			try {
				APIResponse<Void> response = uSCommissionApi.zeroCommission(APIRequest.instance(usUserSet0CommissionRequest));

				if (response ==null
						|| response.getStatus() != APIResponse.Status.OK) {
					log.error("调用report减免手续费失败，userId={},error={}", kycCertificate.getUserId(),response.getErrorData());
				} else {

					log.info("调用report减免手续费成功，userId={}", kycCertificate.getUserId());

				}
			} catch (Exception e){
				log.error("调用report减免手续费失败，userId={},error={}",kycCertificate.getUserId(),e);
			}
		}
	}
}
