package com.binance.account.service.kyc.endHandler;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.service.certificate.impl.UserKycHelper;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.certification.api.KycCertificateApi;
import com.binance.certification.common.model.KycCertificateVo;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import com.binance.platform.common.TrackingUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 降级处理器
 * PASS/FORBID_PASS -> PROCESS(主状态通过，子状态存在不是end状态)
 * PASS/FORBID_PASS -> REFUSED(主状态通过, 子状态全部end且有REFUSED)
 */
@Slf4j
@Service
public class MasterDegradeEndHandler extends AbstractEndHandler {

    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    private UserKycApproveMapper userKycApproveMapper;
    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    
    @Resource
    private KycCertificateApi certificateApi;

    @Override
    public boolean isDoHandler() {
        KycEndContext context = KycEndContext.getContext();
        KycCertificate kycCertificate = context.getKycCertificate();
        if (kycCertificate == null) {
            log.warn("kyc end context error.");
            return false;
        }
        KycCertificateStatus currentStatus = KycCertificateStatus.getByName(kycCertificate.getStatus());
        if (currentStatus != KycCertificateStatus.PASS && currentStatus != KycCertificateStatus.FORBID_PASS) {
            // 不是通过状态的，不能进行降级处理
            return false;
        }
        if (context.hasSomeSubStatus(KycCertificateStatus.PROCESS) || context.hasSomeSubStatus(KycCertificateStatus.REVIEW)) {
            // 子状态存在处理中或者审核中的状态时，需要进行降级处理
            context.setToStatus(KycCertificateStatus.PROCESS);
        	log.info("执行MasterDegradeEndHandler 处理器 userId:{}",kycCertificate.getUserId());
            return true;
        }else if (context.hasSomeSubStatus(KycCertificateStatus.REFUSED)){
        	log.info("执行MasterDegradeEndHandler 处理器 userId:{}",kycCertificate.getUserId());
            context.setToStatus(KycCertificateStatus.REFUSED);
            return true;
        }else {
            // 其他情况下都不能进行降级处理
            return false;
        }
    }

    @Override
    public void handler() {
        KycEndContext context = KycEndContext.getContext();
        KycCertificate kycCertificate = context.getKycCertificate();
        KycCertificateStatus toStatus = context.getToStatus();
        if (kycCertificate == null || toStatus == null) {
            log.error("MasterDegradeEndHandler => get kycCertificate or toStatus fail. userId:{} kycType:{}", context.getUserId(), context.getKycType());
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = context.getUserId();
        KycCertificateKycType kycType = context.getKycType();
        KycCertificateStatus currentStatus = KycCertificateStatus.getByName(kycCertificate.getStatus());
        log.info("MasterDegradeEndHandler => kyc process degrade userId:{} kycType:{} from {} to {}",
                context.getUserId(), context.getKycType(), currentStatus, toStatus);
        String messageTips = null;
        if (toStatus == KycCertificateStatus.REFUSED) {
            messageTips = context.getFirstSubMessageTips();
        }
        // 1. 变更状态
        int count = updateKycFlowStatus(userId, KycCertificateKycLevel.L0.getCode(), toStatus, messageTips);
        log.info("kyc degrade update kyc status userId:{} kycType:{} count:{}", userId, kycType, count);
        kycCertificate.setStatus(toStatus.name());
        kycCertificate.setMessageTips(messageTips);
        // 2. 如果是个人认证，删除idNumberIndexMap
        if (KycCertificateKycType.USER == kycType) {
            iUserCertificate.removeCertificateIndex(userId, null, context.getCountry(), context.getIdNumber(), context.getDocumentType());
        }

        // 3. 变更用户安全等级和状态信息(如果是不合规国籍通过的不需要变更用户等级信息)
        if (currentStatus == KycCertificateStatus.PASS) {
            int userStatusChange = iUserCertificate.updateCertificateStatus(userId, false);
            log.info("kyc degrade update user status. userId:{} kycType:{} count:{}", userId, kycType, userStatusChange);
            int securityLevel = super.updateSecurityLevel(userId, 1);
            log.info("kyc degrade update security level to 1. userId:{} kycType:{} count:{}", userId, kycType, securityLevel);
            //发送MQ消息强制修改用户提币额度
            sendForceWithdrawLimitMessage(userId);
            // 如果是企业认证的，关闭子母账户功能
            if (KycCertificateKycType.COMPANY == kycType) {
                super.disableSubUserFunction(userId);
            }
        }
        // 4.清除用户做人脸识别的正式图片地址
        iFace.removeFaceReferenceRefImage(userId);
        // 5. 清除下kyc的缓存信息
        UserKycHelper.clearKycCountryCache(userId);
        // 6. 发送降级邮件信息
        String emailTemplate = KycCertificateKycType.USER == context.getKycType() ? Constant.JUMIO_KYC_CHECK_FAIL : Constant.JUMIO_COMPANY_CHECK_FAIL;
        super.sendLevelChangeEmail(userId, context.getLanguage(), kycCertificate.getMessageTips(), emailTemplate,  "Kyc认证降级邮件");
        
        UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
        if (userKycApprove != null) {
            userKycApproveMapper.deleteByPrimaryKey(userId);
            log.info("kyc 降级 删除用户的 kyc approve 信息. userId:{}", userId);
        }
        
        try {
        	log.info("kyc 降级 触发riskRating. userId:{} kycStatus:{}",userId,kycCertificate.getStatus());
        	RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
			event.setKycCertificate(kycCertificate);
			event.setTraceId(TrackingUtils.getTrace());
			applicationEventPublisher.publishEvent(event);
        	
        }catch(Exception e) {
        	log.warn("kyc 降级 修改riskRating异常 userId:{}",userId,e);
        }
        
        certificateToSendKycChangeMsg(kycCertificate);
    }

    private void sendForceWithdrawLimitMessage(Long userId) {
        log.info("KYC认证降级，强制修改用户提币额度, userId:{} ", userId);
        Map<String, Object> dataMsg = Maps.newHashMap();
        dataMsg.put("userId", userId);
        dataMsg.put("level", 1);
        dataMsg.put("withdrawLimit", null);
        MsgNotification msg2 =
                new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.WITHDRAW_LIMIT, dataMsg);
        log.info("iMsgNotification set withdraw limit null:{}", JSON.toJSONString(msg2));
        this.iMsgNotification.send(msg2);
    }

    private void certificateToSendKycChangeMsg(KycCertificate kycCertificate) {
    	if(!KycCertificateStatus.REFUSED.name().equals(kycCertificate.getStatus())) {
    		return;
    	}
    	try {
    		KycCertificateVo certificate = new KycCertificateVo();
    		BeanUtils.copyProperties(kycCertificate, certificate);
    		certificate.setKycType(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType()) ? com.binance.certification.common.enums.KycCertificateKycType.COMPANY:
    			com.binance.certification.common.enums.KycCertificateKycType.USER);
    		certificateApi.sendKycChangeMq(APIRequest.instance(certificate));
    	}catch(Exception e) {
    		log.warn("调用certificate发送kyc mq异常 userId:{}",kycCertificate.getUserId(),e);
    	}
    }
}
