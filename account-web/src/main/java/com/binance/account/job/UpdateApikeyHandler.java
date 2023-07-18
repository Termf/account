package com.binance.account.job;

import com.binance.account.service.apimanage.IApiManageService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@JobHandler(value = "updateApikeyHandler")
@Component
public class UpdateApikeyHandler extends IJobHandler {

    @Autowired
    private IApiManageService apiManageService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try {
            apiManageService.updateDBApikey();
        } catch (Exception e) {
            log.error("updateApikeyHandler error:{}", e);
            XxlJobLogger.log(e);
            return FAIL;
        }

        return SUCCESS;
    }
}
