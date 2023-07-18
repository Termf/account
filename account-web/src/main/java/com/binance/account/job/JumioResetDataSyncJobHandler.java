package com.binance.account.job;

import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.mq.JumioInfoMsgListener;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author liliang1
 * @date 2019-03-26 14:10
 */
@Log4j2
@JobHandler(value = "JumioResetDataSyncJobHandler")
@Component
public class JumioResetDataSyncJobHandler extends IJobHandler {

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private JumioInfoMsgListener jumioInfoMsgListener;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("START-JumioResetDataSyncJobHandler");
        log.info("START-JumioResetDataSyncJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            handler(s);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("JumioResetDataSyncJobHandler error-->{0}", e);
            log.error("JumioResetDataSyncJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-JumioResetDataSyncJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-JumioResetDataSyncJobHandler use {}", stopWatch.getTotalTimeSeconds());
        }
    }

    private int parseParam(String param) {
        int defaultHour = 1;
        if (StringUtils.isNotBlank(param) && NumberUtils.isCreatable(param)) {
            int hour = NumberUtils.createInteger(param);
            if (hour <= 0) {
                hour = 1;
            }
            defaultHour = hour;
        }
        return defaultHour;
    }

    private void handler(String param) {
        // 只查询多少小时之前的数据，并且只查询最近15天的数据
        int hours = parseParam(param);
        Date startTime = DateUtils.getNewUTCDateAddDay(-15);
        Date endTime = DateUtils.getNewUTCDateAddHour(-hours);
        // 查询这个时间段内，已经申请过JUMIO但JUMIO未有结果的数据
        List<UserSecurityReset> resets = userSecurityResetMapper.getNeedCheckJumioResults(startTime, endTime);
        if (resets == null || resets.isEmpty()) {
            return;
        }
        XxlJobLogger.log("获取到当前重置流程需要同步的JUMIO数据共:{0}条", resets.size());
        for (UserSecurityReset reset : resets) {
            TrackingUtils.putTracking("RESET_JUMIO_SYNC", UUID.randomUUID().toString().replaceAll("-", ""));
            try {
                process(reset);
            }catch (Exception e) {
                log.error("同步-> 重置流程的JUMIO结果数据异常. userId:{} resetId:{}", reset.getUserId(), reset.getId(), e);
            }finally {
                TrackingUtils.removeTracking();
            }
        }
    }

    private void process(UserSecurityReset reset) {
        String scanRef = reset.getScanReference();
        Long userId = reset.getUserId();
        String resetId = reset.getId();
        JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, scanRef, reset.getType().name());
        if (jumioInfoVo == null) {
            log.info("当前JUMIO结果获取失败. userId:{} scanRef:{} resetId:{}", userId, scanRef, resetId);
            return;
        }
        if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
            log.info("当前JUMIO还未到最终状态，不处理. userId:{} scanRef:{} resetId:{}", userId, scanRef, resetId);
        }else {
            log.info("当前重置流程的JUMIO已经进入终态，进行后续逻辑. userId:{} scanRef:{} resetId:{}", userId, scanRef, resetId);
            String result = jumioInfoMsgListener.execute(jumioInfoVo);
            log.info("重置流程同步JUMIO结果: userId:{} resetId:{} result:{}", userId, resetId, result);
            XxlJobLogger.log("重置流程同步JUMIO结果: userId:{0} resetId:{1} result:{2}", userId, resetId, result);
        }
    }
}
