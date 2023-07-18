package com.binance.account.job;

import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.service.certificate.impl.UserKycDataMigrationBusiness;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@JobHandler(value = "KycCheckCanDoFace")
@Component
public class KycCheckCanDoFace extends IJobHandler {

    @Resource
    private UserKycDataMigrationBusiness userKycDataMigrationBusiness;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int start = 0;
        int rows = 500;
        try {
            List<UserKycApprove> list = new ArrayList<>();
            if (StringUtils.isBlank(param)) {
                list = userKycDataMigrationBusiness.selectFaceCheckPage(null, start, rows, null);
            }else if (param.startsWith("code:")) {
                list = userKycDataMigrationBusiness.selectFaceCheckPage(param.replace("code:", ""), start, rows, null);
            }else if (param.startsWith("user:")) {
                Long userId = Long.parseLong(param.replace("user:", ""));
                list = userKycDataMigrationBusiness.selectFaceCheckPage(null, start, rows, userId);
            }
            for (UserKycApprove userKycApprove : list) {
                String result = userKycDataMigrationBusiness.checkKycFaceCheck(userKycApprove);
                log.info("KycCheckCanDoFace => result {} {}", userKycApprove.getUserId(), result);
            }
            return ReturnT.SUCCESS;
        }catch (Exception e) {
            log.error("KycCheckCanDoFace => error ", e);
            return ReturnT.FAIL;
        }finally {
            stopWatch.stop();
            log.info("KycCheckCanDoFace => use time {}", stopWatch.getTotalTimeSeconds());
        }
    }
}
