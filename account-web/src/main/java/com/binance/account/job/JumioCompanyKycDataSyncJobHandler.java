package com.binance.account.job;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.JumioHandlerType;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.mq.JumioInfoMsgListener;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 企业KYC的 Jumio 数据同步补偿任务
 * @author liliang1
 * @date 2019-04-16 14:47
 */
@Log4j2
@JobHandler(value = "JumioCompanyKycDataSyncJobHandler")
@Component
public class JumioCompanyKycDataSyncJobHandler extends IJobHandler {

    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    @Resource
    private IUserCertificate userCertificate;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private JumioInfoMsgListener jumioInfoMsgListener;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("START-JumioCompanyKycDataSyncJobHandler");
        log.info("START-JumioCompanyKycDataSyncJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            handler(s);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("JumioCompanyKycDataSyncJobHandler error-->{0}", e);
            log.error("JumioCompanyKycDataSyncJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-JumioCompanyKycDataSyncJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-JumioCompanyKycDataSyncJobHandler use {}", stopWatch.getTotalTimeSeconds());
        }
    }

    /**
     * 解析参数为时间，查询 update 时间再这个时间之前的数据
     * @param param
     * @return
     */
    private int parseParamsToMinutes(String param) {
        int minute = 60;
        if (StringUtils.isNotBlank(param) && NumberUtils.isCreatable(param)) {
            minute = NumberUtils.toInt(param);
        }
        if (minute <= 0) {
            minute = 60;
        }
        return minute;
    }

    private void handler(String param) {
        int minutes = parseParamsToMinutes(param);
        // 开始时间全部设置为2个月内的数据
        Date startTime = DateUtils.add(DateUtils.getNewUTCDate(), Calendar.DAY_OF_MONTH, -30);
        Date endTime = DateUtils.add(DateUtils.getNewUTCDate(), Calendar.MINUTE, -minutes);
        List<CompanyCertificate> list = companyCertificateMapper.getExpiredCheckData(startTime, endTime);
        if (list == null || list.isEmpty()) {
            return;
        }
        XxlJobLogger.log("企业KYC数据同步=>时间: {0} -> {1} 共{2}条记录需要同步数据.", startTime, endTime, list.size());
        log.info("企业KYC数据同步=>时间: {} -> {} 共{}条记录需要同步数据.", startTime, endTime, list.size());
        for (CompanyCertificate certificate : list) {
            String trackingId = UUID.randomUUID().toString().replace("-", "");
            TrackingUtils.putTracking("COMPANY_KYC_JUMIO_SYNC", trackingId);
            Long userId = certificate.getUserId();
            Long kycId = certificate.getId();
            try {
                companyKycExpiredSyncHandler(userId, kycId);
            }catch (Exception e) {
                XxlJobLogger.log("企业KYC数据同步=>异常: userId:{0} kycId:{1} {2}", userId, kycId, e);
                log.warn("企业KYC数据同步=>异常: userId:{} kycId:{} ", userId, kycId, e);
            }finally {
                TrackingUtils.removeTracking();
            }
        }
    }

    private void companyKycExpiredSyncHandler(Long userId, Long kycId) {
        CompanyCertificate certificate = userCertificate.getCompanyKycFromMasterDbById(userId, kycId);
        if (certificate == null || !CompanyCertificateStatus.pending.equals(certificate.getStatus())) {
            // 状态不是再最初的状态下，不做任何处理
            return;
        }
        // 如果这笔记录的scanReferent是空值的话，直接设置为过期
        if (StringUtils.isBlank(certificate.getScanReference())) {
            // 直接让这笔userKyc记录过期
            log.info("企业KYC数据同步=>企业KYC长时间未上传过期: userId:{} kycId:{}", userId, kycId);
            userCertificate.companyKycExpired(certificate);
            return;
        }
        // 如果未过期的，直接进行JUMIO数据同步
        String scanRef = certificate.getScanReference();
        JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, scanRef, JumioHandlerType.COMPANY_KYC.getCode());
        if (jumioInfoVo == null) {
            return;
        }
        if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
            log.info("企业KYC数据同步=>当前用户认证未完成，需等待: userId:{} kycId:{}", userId, kycId);
            return;
        }
        XxlJobLogger.log("企业KYC数据同步=>当前JUMIO信息已经有处理结果, 进行数据同步, userId:{0} kycId:{1}", userId, kycId);
        log.info("企业KYC数据同步=>当前JUMIO信息已经有处理结果, 进行数据同步, userId:{} kycId:{}", userId, kycId);
        jumioInfoMsgListener.execute(jumioInfoVo);
    }
}
