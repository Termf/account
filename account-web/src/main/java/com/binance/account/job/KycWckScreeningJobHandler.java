package com.binance.account.job;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.service.certificate.impl.UserWckBusiness;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.request.JumioBizIdRequest;
import com.binance.inspector.vo.worldcheck.WckResultProfileVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * world-check case扫描、自动处理job Created by Shining.Cai on 2018/09/12. updated by Freeman.Li--以下情况自动通过
 * 6. POLITICAL INDIVIDUAL: PEP, 7. DIPLOMAT: PEP 8. MILITARY: LE, PEP, 9. LEGAL: PEP
 **/
@Log4j2
@JobHandler(value = "kycWckScreeningJobHandler")
@Component
public class KycWckScreeningJobHandler extends IJobHandler {



    @Autowired
    private UserWckAuditMapper wckAuditMapper;
    @Autowired
    private UserKycBusiness userKycBusiness;
    @Autowired
    private UserWckBusiness userWckBusiness;
    @Autowired
    private JumioApi jumioApi;
    @Autowired
    private UserKycMapper userKycMapper;
    @Autowired
    private ApolloCommonConfig apolloCommonConfig;

    @Override
    public ReturnT<String> execute(String param) {

        // 默认跑最近6小时的数据
        int minitue = 360;
        if (StringUtils.isNumeric(param)) {
            minitue = Integer.valueOf(param);
        }
        Date now = new Date();
        List<UserWckAudit> list = wckAuditMapper.selectInitialRows(DateUtils.addMinutes(now, -minitue), now);
        if (CollectionUtils.isEmpty(list)) {
            XxlJobLogger.log("没有待处理数据");
            return SUCCESS;
        }
        log.info("KycWckScreeningJobHandler pool thread count={}, the list size = {}",
                apolloCommonConfig.getWckThreadsNum(), list.size());
        ExecutorService pool = Executors.newFixedThreadPool(apolloCommonConfig.getWckThreadsNum());
        list.forEach(item -> {
            try {
                pool.execute(new Runnable() {
                    public void run() {
                        TrackingUtils.putTracking("wckscrnjob", StringUtils.getTimestampRandom32());
                        syncCaseScreeningStatus(item);
                    }
                });

            } catch (Exception e) {
                log.error("KycWckScreeningJobHandler.syncCaseScreeningStatus error, data:{}", item, e);
            }
        });
        pool.shutdown();

        return SUCCESS;
    }

    /**
     * 1.有screening结果，UserWckAudit状态更新为待一审 2.无screening结果，查询worldcheck端的扫描事件是否结束 2.1 扫描未结束，不做处理 2.2
     * 扫描已结束，但无结果，且jumio信息与填写信息一致，则直接返回
     */
    public void syncCaseScreeningStatus(UserWckAudit audit) {
        // 可以进行人工审核
        boolean manualAudit = false;
        // 直接通过，无需人工审核
        boolean directPass = false;
        log.info("syncCaseScreeningStatus userid={} threadid={}", audit.getUserId(), Thread.currentThread().getName());
        List<WckResultProfileVo> result = userWckBusiness.getWckResultProfile(audit.getKycId());

        if (CollectionUtils.isNotEmpty(result)) {
            if (apolloCommonConfig.isWckForceStrictRule()) {
                manualAudit = true;
            } else {
                for (WckResultProfileVo vo : result) {
                    if (!checkWckResultPass(vo.getCategory())) {
                        manualAudit = true;
                        break;
                    }
                }
                if (!manualAudit) {
                    directPass = true;
                }
            }
        } else {
            List auditEvents = userWckBusiness.getWcAuditEvents(audit.getKycId());
            // 扫描事件已执行
            if (CollectionUtils.isNotEmpty(auditEvents)) {

                UserKyc kyc = userKycMapper.getById(audit.getUserId(), audit.getKycId());
                APIResponse<JumioInfoVo> response = jumioApi.getByBizId(APIRequest
                        .instance(new JumioBizIdRequest(audit.getUserId(), audit.getKycId().toString(), "user")));
                if (kyc != null && response.getData() != null) {
                    //firstname 和 lastname 一致就可以直接通过
                    String regularExp = StringUtils.join(kyc.getBaseInfo().getFirstName().toUpperCase(), " .* ",kyc.getBaseInfo().getLastName().toUpperCase());
                   if(kyc.getFillName().equalsIgnoreCase(response.getData().getName()) || 
                           response.getData().getName().toUpperCase().matches(regularExp)) {
                       directPass = true;
                   }
    
                } 
            }
            if(!directPass) {
                manualAudit = true;
            }

        }

        if (manualAudit) {
            log.info("syncCaseScreeningStatus userid={},manualAudit", audit.getUserId());
            audit.setStatus(WckStatus.AUDIT_FIRST);
            wckAuditMapper.updateByPrimaryKeySelective(audit);
        } else if (directPass) {
            log.info("syncCaseScreeningStatus userid={},directPass", audit.getUserId());
            audit.setStatus(WckStatus.PASSED);
            wckAuditMapper.updateByPrimaryKeySelective(audit);
            // 更新kyc状态为通过
            log.info("apolloCommonConfig isMainsiteWckBackendSwitch isOpen = {}",
                    apolloCommonConfig.isMainsiteWckBackendSwitch());
            if (!apolloCommonConfig.isMainsiteWckBackendSwitch()) { // 主站不更新原有数据
                KycAuditRequest kycAuditRequest = new KycAuditRequest();
                kycAuditRequest.setId(audit.getKycId());
                kycAuditRequest.setUserId(audit.getUserId());
                kycAuditRequest.setStatus(KycStatus.wckPassed);
                kycAuditRequest.setMemo("World-Check One passed(non-data)");
                APIResponse response = userKycBusiness.audit(APIRequest.instance(kycAuditRequest));
                XxlJobLogger.log("更新kyc状态Response: {0}", response.toString());
            }
        } else {
            XxlJobLogger.log("状态暂无更新: {0}", audit.getKycId());
        }
    }

    /**
     * 1. TERRORISM: LE, S, OB 2. CRIME-NARCOTIC: LE, S 3. CRIME-FINANCIAL: LE 4. CRIME-OTHER: LE 5.
     * INDIVIDUAL: OB, RE, LE, PEP 6. POLITICAL INDIVIDUAL: PEP, S 7. DIPLOMAT: PEP 8. MILITARY: LE,
     * PEP, S 9. LEGAL: PEP
     * 
     * @param category
     * @return
     */

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
