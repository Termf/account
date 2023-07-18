package com.binance.account.job;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.service.certificate.impl.NewUserWckBusiness;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.platform.common.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author mikiya.chen
 * @date 2020/3/17 2:26 下午
 */
@Log4j2
@JobHandler(value = "KycCheckOutWckManualApplyJobHandler")
@Component
public class KycCheckOutWckManualApplyJobHandler extends IJobHandler {

    @Resource
    UserChannelRiskRatingMapper userChannelRiskRatingMapper;
    @Resource
    KycCertificateMapper kycCertificateMapper;
    @Resource
    KycFillInfoMapper kycFillInfoMapper;
    @Resource
    UserKycApproveMapper userKycApproveMapper;
    @Resource
    private NewUserWckBusiness newUserWckBusiness;
    @Resource
	ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("开始执行 KycCheckOutWckManualApplyJobHandler 执行参数:" + param);
        log.info("START-KycCheckOutWckManualApplyJobHandler");
        if (StringUtils.isBlank(param)) {
            log.info("The input param is illegal, the param is:{}", param);
            return SUCCESS;
        }
        String[] userIds = param.split(",");
        for(int i = 0; i < userIds.length; i++){
            String userIdStr = userIds[i];
            if (StringUtils.isBlank(userIdStr) || !StringUtils.isNumeric(userIdStr)) {
                log.info("one of input userId is illegal, the userId is:{}", userIdStr);
                continue;
            }
            Long userId = Long.parseLong(userIdStr);
            applyChannelWckByUserId(userId);
        }
        return null;
    }

    private void applyChannelWckByUserId(Long userId){
        UserChannelRiskRating userChannelRiskRating = userChannelRiskRatingMapper.selectByUk(userId, "CHECKOUT");
        if(userChannelRiskRating == null){
            log.info("the user has no recode in checkout risk rating, the userId is:{}", userId);
            return;
        }
        // 判断kyc情况
        KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
        if (kycCertificate == null) {
            log.info("the user has no recode in kycCertificate, the userId is:{}", userId);
            return;
        }
        KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId,
                KycFillType.BASE.name());

        // base || kyc通过，添加记录+上送wck
        if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
            if (kycFillInfo != null) {
                StringBuffer nameBuff = new StringBuffer()
                        .append(StringUtils.isBlank(kycFillInfo.getFirstName()) ? "" : kycFillInfo.getFirstName().trim())
                        .append(StringUtils.isBlank(kycFillInfo.getMiddleName()) ? ""
                                : " " + kycFillInfo.getMiddleName().trim())
                        .append(StringUtils.isBlank(kycFillInfo.getLastName()) ? ""
                                : " " + kycFillInfo.getLastName().trim());
                String name = nameBuff.toString();
                String birthday = kycFillInfo.getBirthday();
                String country = kycFillInfo.getCountry();
                UserChannelWckAuditVo auditVo = newUserWckBusiness.applyWorldCheck(userId, "BASE",
                        name.toString().trim(), birthday, country);
                if (auditVo != null) {
                	RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
                	event.setTraceId(TrackingUtils.getTrace());
                	event.setUserChannelWckAuditVo(auditVo);
                	event.setKycCertificate(kycCertificate);
                	applicationEventPublisher.publishEvent(event);
                }
            }
        }
        if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
            UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userChannelRiskRating.getUserId());
            if (userKycApprove != null) {

                StringBuffer nameBuff = new StringBuffer()
                        .append((StringUtils.isBlank(userKycApprove.getCertificateFirstName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateFirstName()))  ? ""
                                : userKycApprove.getCertificateFirstName().trim().replaceAll("N/A", ""))
                        .append((StringUtils.isBlank(userKycApprove.getCertificateLastName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateLastName())) ? ""
                                : " " + userKycApprove.getCertificateLastName().trim().replaceAll("N/A", ""));
                String dob = ((StringUtils.isBlank(userKycApprove.getCertificateDob()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateDob())) ? ""
                        : userKycApprove.getCertificateDob()).trim().replaceAll("N/A", "");
                String certificateCountry = ((StringUtils.isBlank(userKycApprove.getCertificateCountry()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateCountry())) ? ""
                        : userKycApprove.getCertificateCountry()).trim().replaceAll("N/A", "");
                String name = nameBuff.toString().replaceAll("N/A", "").trim();
                UserChannelWckAuditVo auditVo = newUserWckBusiness.applyWorldCheck(userId, "KYC",
                        name.toString().trim(), dob, certificateCountry);
                if (auditVo != null) {
                    RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
                	event.setTraceId(TrackingUtils.getTrace());
                	event.setUserChannelWckAuditVo(auditVo);
                	event.setKycCertificate(kycCertificate);
                	applicationEventPublisher.publishEvent(event);
                }
            }
        }
    }
}
