package com.binance.account.job;

import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yangyang on 2019/6/17.
 */
@Log4j2
@JobHandler(value = "updateMbxBurnFeeStatusJobHandler")
@Component
public class UpdateMbxBurnFeeStatusJobHandler extends IJobHandler{

    @Autowired
    private AccountApiClient accountApiClient;

    /**
     * s = userId,1/0
     * @param s
     * @return
     * @throws Exception
     */

    @Override
    @Trace
    public ReturnT<String> execute(String s) throws Exception {
        TrackingUtils.saveTraceId();
        log.info("updateMbxBurnFeeStatusJobHandler.execute.start.param:{}",s);
        if (StringUtils.isBlank(s)){
            return SUCCESS;
        }
        String[] splits = s.split(",");
        if (splits == null || splits.length != 2){
            return SUCCESS;
        }
        log.info("updateMbxBurnFeeStatusJobHandler.splits:{}",splits);
        accountApiClient.setGas(splits[0],Integer.parseInt(splits[1]) == 1);

        log.info("updateMbxBurnFeeStatusJobHandler.execute.end");
        return SUCCESS;
    }

}
