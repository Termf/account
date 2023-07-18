package com.binance.account.job;

import com.binance.account.service.operationlog.IUserOperationLog;
import com.binance.account.vo.security.request.CountLoginRequest;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * 统计用户登陆超时，加入缓存
 */
@Log4j2
@JobHandler(value = "countLoginJob")
@Component
public class CountLoginJob extends IJobHandler {

    @Autowired
    private IUserOperationLog userOperationLog;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try{
            CountLoginRequest request = new CountLoginRequest();
            Date yesterday = DateUtils.addDays(new Date(), -1);
            request.setStartTime(DateUtils.getDateBegin(yesterday));
            request.setEndTime(DateUtils.getDateEnd(yesterday));
            log.info("count login job start, param:{}", request);
            String key = "stat.pa."+request.getStartTime().getTime()/1000+"."+request.getEndTime().getTime()/1000;
            RedisCacheUtils.del(key);
            userOperationLog.countDistinctLogin(request);
            log.info("count login job done");
            return ReturnT.SUCCESS;
        }catch (Exception e){
            log.warn("count login job exception", e);
            return ReturnT.FAIL;
        }
    }

}
