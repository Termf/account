package com.binance.account.job;

import com.binance.account.data.entity.certificate.KycCnIdCard;
import com.binance.account.data.mapper.certificate.KycCnIdCardMapper;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Log4j2
@JobHandler(value = "KycCnUserCreateC2cAccountJobHandler")
@Component
public class KycCnUserCreateC2cAccountJobHandler extends IJobHandler {

    @Resource
    private KycCnIdCardMapper kycCnIdCardMapper;
    @Resource
    private UserBusiness userBusiness;


    @Value("${kyc.cnIdcard.ocrJob.size:200}")
    private int pageSize;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("开始执行 KycCnUserCreateC2cAccountJobHandler 执行参数:" + param);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Integer flag = null;
            if (StringUtils.isNotBlank(param) && NumberUtils.isCreatable(param)) {
                flag = NumberUtils.createInteger(param);
            }
            handler(flag);
            return ReturnT.SUCCESS;
        }catch (Exception e) {
            XxlJobLogger.log("执行 KycCnUserCreateC2cAccountJobHandler 失败 param:{0} {1}", param, e);
            log.error("执行 KycCnUserCreateC2cAccountJobHandler 失败 param:{}", param, e);
            return FAIL;
        }finally {
            stopWatch.stop();
            XxlJobLogger.log("执行 KycCnUserCreateC2cAccountJobHandler 完成 use {0}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private void handler(Integer flag) {
        List<KycCnIdCard> idCards = kycCnIdCardMapper.selectCreateFiatAccountList(0, pageSize,  flag);
        XxlJobLogger.log("当前需要进行 KycCnUserCreateC2cAccountJobHandler 条数 {0} ", idCards == null ? 0 : idCards.size());
        if (idCards == null || idCards.isEmpty()) {
            return;
        }
        for (KycCnIdCard idCard : idCards) {
            createFiatAccount(idCard);
        }
    }

    private void createFiatAccount(KycCnIdCard idCard) {
        try {
            TrackingUtils.putTracking("KycCnUserCreateFiatAccount", UUID.randomUUID().toString().replaceAll("-", ""));
            UserIdRequest request = new UserIdRequest();
            request.setUserId(idCard.getUserId());
            userBusiness.createFiatAccount(APIRequest.instance(request), false);
            idCard.setFiatStatus("PASS");
            kycCnIdCardMapper.updateByPrimaryKeySelective(idCard);
        }catch (BusinessException e) {
            log.warn("KycCnUserCreateFiatAccount create fiat account fail. userId:{}", idCard.getUserId());
            idCard.setFiatStatus("FAIL");
            idCard.setFiatRemark(e.getErrorCode().toString());
            kycCnIdCardMapper.updateByPrimaryKeySelective(idCard);
        } catch (Exception e) {
            log.warn("KycCnUserCreateFiatAccount create fiat account error. userId:{}", idCard.getUserId());
            idCard.setFiatStatus("FAIL");
            idCard.setFiatRemark(e.getMessage());
            kycCnIdCardMapper.updateByPrimaryKeySelective(idCard);
        }finally {
            TrackingUtils.removeTracking();
            TrackingUtils.removeTraceId();
        }
    }
}
