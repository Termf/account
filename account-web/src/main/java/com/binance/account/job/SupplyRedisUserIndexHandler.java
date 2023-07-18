package com.binance.account.job;

import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


@Log4j2
@JobHandler(value = "SupplyRedisUserIndexHandler")
@Component
public class SupplyRedisUserIndexHandler extends IJobHandler {

    @Autowired
    private UserIndexMapper userIndexMapper;
    @Value("${account.user.id.alert.value}")
    private long userIdAlertValue;//userid库存少于多少就要生成的阈值


    @Override
    @Trace
	public ReturnT<String> execute(String param) throws Exception {
        TrackingUtils.saveTraceId();
        StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start SupplyRedisUserIndexHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            supplyRedisUserIndex();
            return SUCCESS;
        } catch (Exception e) {
            log.error("SupplyRedisUserIndexHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end SupplyRedisUserIndexHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}

    public void supplyRedisUserIndex() {
        try {
                long count = RedisCacheUtils.count(CacheKeys.REGISTER_USER_ID);
                if (count < this.userIdAlertValue) {
                    List<UserIndex>  userIndexList=userIndexMapper.selectUnusedUserIndex();
                    if(CollectionUtils.isEmpty(userIndexList)){
                        log.info("userIndexList is empty");
                    }
                    for(UserIndex userIndex:userIndexList){
                        Long userId=userIndex.getUserId();
                        log.info("填充userId到redis:{}", userId);
                        RedisCacheUtils.setLeftPush(CacheKeys.REGISTER_USER_ID, userId);
                        log.info("填充userId到redis:{},成功！", userId);
                    }
                }
        } catch (Exception e) {
            log.error(String.format("supplyRedisUserIndex failed,  exception:"), e);
        }
    }



}
