package com.binance.account.job;

import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.master.constant.CacheKeys;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


@Log4j2
@JobHandler(value = "AutoGenerateUserIndexHandler")
@Component
public class AutoGenerateUserIndexHandler extends IJobHandler {

    @Autowired
    private UserCommonBusiness userCommonBusiness;


    @Value("${account.user.id.alert.value}")
    private long userIdAlertValue;//userid库存少于多少就要生成的阈值
    @Value("${account.user.id.alert.generate}")
    protected long autoUserIdGenerateNum; //每次自动生成多少userid
    @Value("${account.init.min.user.id:10000000}")
    private long initMinUserId;//最小的userid是多少
    @Value("10")
    private long manualUserIdGenerateNum;//手动生成多少个


	@Override
    @Trace
	public ReturnT<String> execute(String param) throws Exception {
        TrackingUtils.saveTraceId();
        StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start AutoGenerateUserIndexHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            autoGenerateUserIndexHandler();
            return SUCCESS;
        } catch (Exception e) {
            log.error("AutoGenerateUserIndexHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end AutoGenerateUserIndexHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	public void autoGenerateUserIndexHandler() {
        log.info("userIdAlertValue={},autoUserIdGenerateNum={},initMinUserId={},manualUserIdGenerateNum={}",userIdAlertValue,autoUserIdGenerateNum,initMinUserId,manualUserIdGenerateNum);
        long unusedCount = RedisCacheUtils.count(CacheKeys.REGISTER_USER_ID);
        log.info("当前未使用的UserId数：{}", unusedCount);
        userCommonBusiness.generateUserIndex(autoUserIdGenerateNum);
	}



}
