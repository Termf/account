package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.LanguageEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Log4j2
@Service
public class UsL1ToL0EndHandler extends AbstractEndHandler {

    @Override
    public boolean isDoHandler() {
    	KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
        Integer level = kycCertificate.getKycLevel();
        //Company 不存在L1
        if(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
        	return false;
        }
        
        if (!Objects.equals(KycCertificateKycLevel.L1.getCode(), level)) {
            return false;
        }
        // base fill status is not pass
        return !StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getBaseFillStatus());
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void handler() {
    	KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
        Long userId = kycCertificate.getUserId();
        log.info("US KYC L1 to L0 => 开始处理. userId:{}", userId);
        // 1. kyc 等级 变到 L0
        updateKycFlowStatus(userId, KycCertificateKycLevel.L0.getCode(), KycCertificateStatus.PROCESS, kycCertificate.getMessageTips());
        kycCertificate.setKycLevel(KycCertificateKycLevel.L0.getCode());
        //个人用户维护 kyc_taxid_index
        if(KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
        	KycFillInfo kycFillInfo = getMasterKycFillInfo(kycCertificate.getUserId(), KycFillType.BASE);
        	kycTaxidIndexMapper.deleteByPrimaryKey(kycFillInfo.getTaxId());
        }
        
        // 2. 安全等级变更到 1 级
        updateSecurityLevel(userId, 1);
        // send notify email
        sendLevelChangeEmail(kycCertificate.getUserId(), LanguageEnum.EN_US, kycCertificate.getBaseFillTips(), UserConst.US_KYC_EMAIL_L1_TO_L0, "KYC认证L1降级到L0邮件");
        
        //重置用户邮件通知任务
        iUserKycEmailNotify.reset(userId);
    }
}
