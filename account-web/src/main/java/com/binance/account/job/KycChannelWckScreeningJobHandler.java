package com.binance.account.job;

import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.data.entity.certificate.UserChannelWckAudit;
import com.binance.account.data.mapper.certificate.UserChannelWckAuditMapper;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.service.certificate.impl.NewUserWckBusiness;
import com.binance.inspector.vo.worldcheck.WckResultProfileVo;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.platform.common.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * channel 用户 world check case 扫描
 * 1.有screening结果，UserChannelWckAudit状态更新为待一审 2.无screening结果，查询worldcheck端的扫描事件是否结束 2.1 扫描未结束，不做处理 2.2
 * 扫描已结束，但无结果，则状态变更为AUTO_PASS, 同时更改用户的tier等级
 * @author mikiya.chen
 * @date 2020/3/6 10:59 上午
 */
@Log4j2
@JobHandler(value = "KycChannelWckScreeningJobHandler")
@Component
public class KycChannelWckScreeningJobHandler extends IJobHandler {

    @Resource
    private UserChannelWckAuditMapper userChannelWckAuditMapper;
    @Resource
    private NewUserWckBusiness newUserWckBusiness;
    @Resource
	ApplicationEventPublisher applicationEventPublisher;
    

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("开始执行 KycChannelWckScreeningJobHandler 执行参数:" + param);
        log.info("START-KycChannelWckScreeningJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            // 默认跑最近6小时的数据
            int minitue = 360;
            if (StringUtils.isNumeric(param)) {
                minitue = Integer.valueOf(param);
            }
            Date now = new Date();
            List<UserChannelWckAudit> list = userChannelWckAuditMapper.selectInitialRows(DateUtils.addMinutes(now, -minitue), now);
            if (CollectionUtils.isEmpty(list)) {
                XxlJobLogger.log("没有待处理数据");
                return SUCCESS;
            }
            list.forEach(item -> {
                syncCaseScreeningStatus(item);
            });
            return SUCCESS;
        } catch (Exception e){
            XxlJobLogger.log("KycChannelWckScreeningJobHandler error-->{0}", e);
            log.error("KycChannelWckScreeningJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-KycChannelWckScreeningJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-KycChannelWckScreeningJobHandler use {}s", stopWatch.getTotalTimeSeconds());
        }
    }

    /**
     * 1.有screening结果，且Category不满足自动通过的条件 UserWckAudit状态更新为待一审 2.无screening结果，查询worldcheck端的扫描事件是否结束 2.1 扫描未结束，不做处理 2.2
     * 扫描已结束，但无结果，则直接通过, 同时更改用户的tier等级
     */
    public void syncCaseScreeningStatus(UserChannelWckAudit audit) {
        try {
            // 可以进行人工审核
            boolean manualAudit = false;
            // 直接通过，无需人工审核
            boolean directPass = false;

            List<WckResultProfileVo> result = newUserWckBusiness.getChannelWckResultProfile(audit.getCaseId());
            if (CollectionUtils.isNotEmpty(result)) {
                for (WckResultProfileVo vo : result) {
                    if (!checkWckResultPass(vo.getCategory())) {
                        manualAudit = true;
                        break;
                    }
                }
                if (!manualAudit) {
                    directPass = true;
                }
            } else {
                //检查扫描事件是否完成
                List auditEvents = newUserWckBusiness.getChannelWcAuditEvents(audit.getCaseId());
                //扫描事件已执行 自动通过
                if (CollectionUtils.isNotEmpty(auditEvents)) {
                    directPass = true;
                }
                if (!directPass) {
                    manualAudit = true;
                }
            }

            if (manualAudit) {
                log.info("syncChannelCaseScreeningStatus userid={}, caseId={}, manualAudit", audit.getUserId(), audit.getCaseId());
                audit.setStatus(WckChannelStatus.AUDIT_FIRST);
                userChannelWckAuditMapper.updateByPrimaryKeySelective(audit);
            } else if (directPass) {
                log.info("syncChannelCaseScreeningStatus userid={}, caseId={}, autoPass", audit.getUserId(), audit.getCaseId());
                audit.setStatus(WckChannelStatus.AUTO_PASS);
                userChannelWckAuditMapper.updateByPrimaryKeySelective(audit);
                String traceId = TrackingUtils.getTrace();
                RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
                event.setUserChannelWckAuditVo(newUserWckBusiness.convertWckChannelAuditDoToVo(audit));
                event.setTraceId(traceId);
                event.setUserId(audit.getUserId());
                applicationEventPublisher.publishEvent(event);
            }
        }catch (Exception e){
            log.warn("syncChannelCaseScreeningStatus error, caseId is:{}, exception is:{}", audit.getCaseId(), e);
        }
    }

    private boolean checkWckResultPass(String category) {
        if(StringUtils.isEmpty(category)) {
            return false;
        }
        String categoryUpper = category.toUpperCase();
        if (categoryUpper.contains("POLITICAL INDIVIDUAL:PEP")) {
            return true;
        }
        if (categoryUpper.contains("DIPLOMAT:PEP")) {
            return true;
        }
        if (categoryUpper.contains("MILITARY:LE")) {
            return true;
        }
        if (categoryUpper.contains("MILITARY:PEP")) {
            return true;
        }
        if (categoryUpper.contains("LEGAL:PEP")) {
            return true;
        }

        return false;
    }
}
