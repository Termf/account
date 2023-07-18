package com.binance.account.job;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.query.JumioQuery;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.service.certificate.impl.UserWckBusiness;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import java.util.Date;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * world-check 补偿job
 * Created by Shining.Cai on 2018/09/12.
 **/
@Log4j2
@JobHandler(value = "kycWckRepairJobHandler")
@Component
public class KycWckRepairJobHandler extends IJobHandler {

    @Autowired
    private UserWckAuditMapper wckAuditMapper;
    @Autowired
    private UserKycMapper userKycMapper;
    @Autowired
    private JumioMapper jumioMapper;
    @Autowired
    private UserWckBusiness userWckBusiness;

    @Override
    public ReturnT<String> execute(String param) {
        log.info("KycWckRepairJobHandler starting.. params:{}", param);
        TrackingUtils.putTracking("job", StringUtils.getTimestampRandom32());
        // 默认跑最近24小时的数据
        int hours = 24;
        if (StringUtils.isNumeric(param)) {
            hours = Integer.valueOf(param);
        }
        Date now = new Date();

        JumioQuery query = new JumioQuery();
        query.setStatus(String.valueOf(KycStatus.wckWaiting.ordinal()));
        query.setStartCreateTime(DateUtils.addHours(now, -hours));
        query.setEndCreateTime(now);
        query.setPage(1);
        query.setRows(1000);
        List<UserKyc> kycList = userKycMapper.getList(query);

        if (CollectionUtils.isEmpty(kycList)) {
            log.info("KycWckRepairJobHandler-没有待处理数据");
            return SUCCESS;
        }
        for (UserKyc kyc:kycList){
            UserWckAudit audit = wckAuditMapper.selectByPrimaryKey(kyc.getId());
            if (audit != null){
                log.info("KycWckRepairJobHandler-数据正常，无需修复: {}-{}", kyc.getUserId(), kyc.getId());
                continue;
            }
            Jumio jumio = jumioMapper.selectByPrimaryKey(kyc.getUserId(), kyc.getJumioId());
            if (jumio == null){
                log.warn("KycWckRepairJobHandler-缺失jumio信息:{}", kyc.getId());
            }
            try {
                userWckBusiness.applyWorldCheck(jumio, kyc);
                log.info("KycWckRepairJobHandler-修复成功: {}", kyc.getId());
            } catch (Exception e) {
                log.warn("KycWckRepairJobHandler-applyWorldCheck error:{}, {}", kyc.getId(), e);
            }
        }

        return SUCCESS;
    }
}
