package com.binance.account.job;


import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.certificate.UserKycApprove.BaseInfo;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.service.certificate.impl.UserWckBusiness;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@JobHandler(value = "migrateUserKYCApprove2WCKJobHandler")
@Component
public class MigrateUserKYCApprove2WCKJobHandler extends IJobHandler {

    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private JumioMapper jumioMapper;
    @Autowired
    private UserWckBusiness userWckBusiness;
    @Autowired
    private UserWckAuditMapper wckAuditMapper;
    @Autowired
    private ApolloCommonConfig apolloCommonConfig;

    @Override
    public ReturnT<String> execute(String param) {
        // TODO Auto-generated method stub
        // param 1.single userId; 2.country;3.multiple userids,4.starttime endtime
        // 1. get the data from user_kyc_approve


        try {
            JSONObject jsonObj = null;
            if (StringUtils.isNotEmpty(param)) {
                jsonObj = JSON.parseObject(param);
            } else {// 默认扫描一天的数据
                jsonObj = new JSONObject();
                Date curDate = DateUtils.getNewDate();
                jsonObj.put("startTime",
                        DateUtils.formatter(DateUtils.addDays(curDate, -1), DateUtils.DETAILED_NUMBER_PATTERN));
                jsonObj.put("endTime", DateUtils.formatter(curDate, DateUtils.DETAILED_NUMBER_PATTERN));
            }

            Integer initalCounts = wckAuditMapper.selectCountsByStatus(WckStatus.INITIAL, null, null, null);
            log.info("migrateUserKYCApprove2WCKJobHandler apolloCommonConfig.getMigrateDataLimit={}",
                    apolloCommonConfig.getMigrateDataLimit());
            if (initalCounts > apolloCommonConfig.getMigrateDataLimit()) {
                log.info("user_wck_audit have too many data whose status is inital");
                return new ReturnT<String>(FAIL.getCode(), "user_wck_audit have too many data whose status is inital");
            }
            if (migrateUserKycApprove(jsonObj)) {
                log.info("migrateUserKYCApprove2WCKJobHandler success.");
                return SUCCESS;
            } else {
                log.info("migrateUserKYCApprove2WCKJobHandler there is no personal data");
                return new ReturnT<String>(FAIL.getCode(), "there is no personal data");
            }
        } catch (Exception e) {
            log.error("MigrateUserKYCApprove2WCKJobHandler exception : {},{}", e.getMessage(), e.getCause());
            return new ReturnT<String>(FAIL.getCode(), e.getMessage());
        }
    }

    private boolean migrateUserKycApprove(JSONObject jsonObj) throws Exception {
        List<String> fbCountries = null;
        if (StringUtils.isNotEmpty(apolloCommonConfig.getFilterCountry())) {
            log.info("migrateUserKycApprove apolloCommonConfig filter country : {}",
                    apolloCommonConfig.getFilterCountry());
            String[] filterCountry = apolloCommonConfig.getFilterCountry().split(",");
            fbCountries = Arrays.asList(filterCountry);
        }

        BaseInfo baseInfo = new BaseInfo();
        UserKycApprove query = new UserKycApprove();
        query.setBaseInfo(baseInfo);
        if (jsonObj.containsKey("userId"))
            query.setUserId(jsonObj.getLong("userId"));
        if (jsonObj.containsKey("country")) {
            query.getBaseInfo().setCountry(jsonObj.getString("country"));
        }
        if (jsonObj.containsKey("userIds")) {
            List<Long> userIdList = jsonObj.getJSONArray("userIds").toJavaList(Long.class);
            query.setUserIds(new HashSet<Long>(userIdList));
        }
        if (jsonObj.containsKey("startTime")) {
            query.setStartTime(DateUtils.parseDate(jsonObj.getString("startTime"), DateUtils.DETAILED_NUMBER_PATTERN));
        }
        if (jsonObj.containsKey("endTime")) {
            query.setEndTime(DateUtils.parseDate(jsonObj.getString("endTime"), DateUtils.DETAILED_NUMBER_PATTERN));
        }
        if (jsonObj.containsKey("type")) {
            query.setCertificateType(jsonObj.getInteger("type"));
        }
        List<UserKycApprove> userKycApproveList = userKycApproveMapper.getUserKycApproveList(query);
        if (userKycApproveList == null || userKycApproveList.size() <= 0) {
            log.info("there are no records the param is = {}", query.toString());
            return false;
        }
        for (UserKycApprove userKycApprove : userKycApproveList) {
            try {
                if (userKycApprove.getCertificateId() != null) {
                    Jumio jumio =
                            jumioMapper.selectByPrimaryKey(userKycApprove.getUserId(), userKycApprove.getJumioId());
                    if (jumio != null && (fbCountries == null || !fbCountries.contains(jumio.getIssuingCountry()))) {
                        log.info("migrateUserKycApprove userid={},country={}", userKycApprove.getUserId(),
                                jumio.getIssuingCountry());
                        UserKyc userKyc = new UserKyc();
                        userKyc.setBaseInfo(new UserKyc.BaseInfo());
                        BeanUtils.copyProperties(userKycApprove, userKyc);
                        if (userKycApprove.getBaseInfo() != null) {
                            BeanUtils.copyProperties(userKycApprove.getBaseInfo(), userKyc.getBaseInfo());
                        }
                        userKyc.setId(userKycApprove.getCertificateId());
                        userWckBusiness.applyWorldCheck(jumio, userKyc);
                    }
                }
            } catch (Exception e) {
                log.error("migrateUserKycApprove exception = {}", e.getMessage());
            }
        }
        return true;
    }

}
