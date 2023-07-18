package com.binance.account.job;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.JumioHandlerType;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.mapper.certificate.UserKycMapper;
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
 * 用户KYC的 Jumio 数据同步补偿任务
 * @author liliang1
 * @date 2019-04-16 14:47
 */
@Log4j2
@JobHandler(value = "JumioUserKycDataSynchJobHandler")
@Component
public class JumioUserKycDataSyncJobHandler extends IJobHandler {

    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    private IUserCertificate userCertificate;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private JumioInfoMsgListener jumioInfoMsgListener;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("START-JumioUserKycDataSynchJobHandler");
        log.info("START-JumioUserKycDataSynchJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            handler(s);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("JumioUserKycDataSynchJobHandler error-->{0}", e);
            log.error("JumioUserKycDataSynchJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-JumioUserKycDataSynchJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-JumioUserKycDataSynchJobHandler use {}", stopWatch.getTotalTimeSeconds());
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
        List<UserKyc> list = userKycMapper.getExpiredCheckData(startTime, endTime);
        if (list == null || list.isEmpty()) {
            return;
        }
        XxlJobLogger.log("用户KYC数据同步=>时间: {0} -> {1} 共{2}条记录需要同步数据.", startTime, endTime, list.size());
        log.info("用户KYC数据同步=>时间: {} -> {} 共{}条记录需要同步数据.", startTime, endTime, list.size());
        for (UserKyc userKyc : list) {
            String trackingId = UUID.randomUUID().toString().replace("-", "");
            TrackingUtils.putTracking("USER_KYC_JUMIO_SYNC", trackingId);
            Long userId = userKyc.getUserId();
            Long kycId = userKyc.getId();
            try {
                userKycExpiredSyncHandler(userId, kycId);
            }catch (Exception e) {
                XxlJobLogger.log("用户KYC数据同步=>异常: userId:{0} kycId:{1} {2}", userId, kycId, e);
                log.warn("用户KYC数据同步=>异常: userId:{} kycId:{} ", userId, kycId, e);
            }finally {
                TrackingUtils.removeTracking();
            }
        }
    }

    private void userKycExpiredSyncHandler(Long userId, Long kycId) {
        UserKyc userKyc = userCertificate.getUserKycFromMasterDbById(userId, kycId);
        if (userKyc == null || !KycStatus.pending.equals(userKyc.getStatus())) {
            // 状态不是再最初的状态下，不做任何处理
            return;
        }
        // 如果这笔记录的scanReferent是空值的话，直接设置为过期
        if (StringUtils.isBlank(userKyc.getScanReference())) {
            // 直接让这笔userKyc记录过期
            log.info("用户KYC数据同步=>用户KYC长时间未上传过期: userId:{} kycId:{}", userId, kycId);
            userCertificate.userKycExpired(userKyc);
            return;
        }
        // 如果未过期的，直接进行JUMIO数据同步
        String scanRef = userKyc.getScanReference();
        JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, scanRef, JumioHandlerType.USER_KYC.getCode());
        if (jumioInfoVo == null) {
            return;
        }
        if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
            log.info("用户KYC数据同步=>当前用户认证未完成，需等待: userId:{} kycId:{}", userId, kycId);
            return;
        }
        XxlJobLogger.log("用户KYC数据同步=>当前JUMIO信息已经有处理结果, 进行数据同步, userId:{0} kycId:{1}", userId, kycId);
        log.info("用户KYC数据同步=>当前JUMIO信息已经有处理结果, 进行数据同步, userId:{} kycId:{}", userId, kycId);
        jumioInfoMsgListener.execute(jumioInfoVo);
    }
}
