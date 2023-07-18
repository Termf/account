package com.binance.account.service.kyc.endHandler;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.platform.common.TrackingUtils;

import lombok.extern.log4j.Log4j2;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平等迁移状态, 不会涉及到降级和升级操作
 * PROCESS -> REFUSED (主状态处理中，所有子状态end且存在拒绝)
 * REFUSED -> PROCESS (主状态拒绝，子状态存在处理中)
 */
@Log4j2
@Service
public class MasterEqualExchangeEndHandler extends AbstractEndHandler {
	

    @Override
    public boolean isDoHandler() {
        KycEndContext context = KycEndContext.getContext();
        KycCertificate kycCertificate = context.getKycCertificate();
        if (kycCertificate == null) {
            log.warn("kyc end context error.");
            return false;
        }
        KycCertificateStatus currentStatus = KycCertificateStatus.getByName(kycCertificate.getStatus());
        if (KycCertificateStatus.PROCESS == currentStatus && KycCertificateStatus.isEndStatus(context.getBasicStatus())
                && KycCertificateStatus.isEndStatus(context.getFaceStatus())) {
        	
            // 看是否符合所有子状态都是end，且存在拒绝状态
            if (context.isOcrFlow()) {
            	if( !KycCertificateStatus.isEndStatus(context.getFaceOrcStatus())) {
            		// ocr 流程，但是ocr 状态还未到end
            		return false;
            	}
            }else if (!KycCertificateStatus.isEndStatus(context.getJumioStatus())) {
                return false;
            }
            if (KycCertificateKycType.COMPANY == context.getKycType() && !KycCertificateStatus.isEndStatus(context.getGoogleFormStatus())) {
                return false;
            }
            // 所有状态都进入了end的状态下，看是否存在有拒绝状态的
            if (context.hasSomeSubStatus(KycCertificateStatus.REFUSED)) {
                context.setToStatus(KycCertificateStatus.REFUSED); // 设置下转化后的状态，方便处理逻辑中直接获取
                log.info("MasterEqualExchangeEndHandler 当前kyc用户为存在RESUSED，执行处理器userId:{}", kycCertificate.getUserId());
                return true;
            }
        }else if (KycCertificateStatus.REFUSED == currentStatus
                && (context.hasSomeSubStatus(KycCertificateStatus.PROCESS) || context.hasSomeSubStatus(KycCertificateStatus.REVIEW))) {
            // 主状态是拒绝，且存在有处理中的状态时，需要变迁到处理中的状态
            context.setToStatus(KycCertificateStatus.PROCESS);
            log.info("执行MasterEqualExchangeEndHandler 处理器 userId:{}",kycCertificate.getUserId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void handler() {
        KycEndContext context = KycEndContext.getContext();
        KycCertificate kycCertificate = context.getKycCertificate();
        KycCertificateStatus toStatus = context.getToStatus();
        if (kycCertificate == null || toStatus == null) {
            log.error("MasterEqualExchangeEndHandler => get kycCertificate or toStatus fail. userId:{} kycType:{}", context.getUserId(), context.getKycType());
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = context.getUserId();
        String currentStatus = kycCertificate.getStatus();
        String messageTps = null;
        if (KycCertificateStatus.REFUSED == toStatus) {
            messageTps = context.getFirstSubMessageTips();
        }
        int count = updateKycFlowStatus(userId, null, toStatus, messageTps);
        kycCertificate.setMessageTips(messageTps);
        log.info("MasterEqualExchangeEndHandler => just change kycCertificate status. userId:{} kycType:{} from {} to {} result {}",
                userId, context.getKycType(), currentStatus, toStatus, count);
        if (KycCertificateStatus.REFUSED == toStatus) {
            // 变更到拒绝状态时，需要发送通知邮件
            // 5. 发送变更邮件
            String emailTemplate = KycCertificateKycType.USER == context.getKycType() ? Constant.JUMIO_KYC_CHECK_FAIL : Constant.JUMIO_COMPANY_CHECK_FAIL;
            super.sendLevelChangeEmail(userId, context.getLanguage(), kycCertificate.getMessageTips(), emailTemplate,  "Kyc认证拒绝邮件");
        }
        
        try {
        	RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
			event.setKycCertificate(kycCertificate);
			event.setTraceId(TrackingUtils.getTrace());
			applicationEventPublisher.publishEvent(event);
        }catch(Exception e) {
        	log.warn("kyc 降级 修改riskRating异常 userId:{}",userId,e);
        }
    }
}
