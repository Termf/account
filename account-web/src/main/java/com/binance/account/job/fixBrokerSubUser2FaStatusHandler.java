package com.binance.account.job;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.master.constant.Constant;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.List;


@Log4j2
@JobHandler(value = "fixBrokerSubUser2FaStatusHandler")
@Component
public class fixBrokerSubUser2FaStatusHandler extends IJobHandler {

    @Autowired
    protected UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;


	@Override
    @Trace
	public ReturnT<String> execute(String param) throws Exception {
        TrackingUtils.saveTraceId();
        StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start fixBrokerSubUser2FaStatusHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            fixBrokerSubUser2FaStatus();
            return SUCCESS;
        } catch (Exception e) {
            log.error("fixBrokerSubUser2FaStatusHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end fixBrokerSubUser2FaStatusHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	public void fixBrokerSubUser2FaStatus() {
        List<Long> userIds = userMapper.selectBrokerSubUserId();
        if(CollectionUtils.isEmpty(userIds)){
            log.info("fixBrokerSubUser2FaStatus userIds is empty");
            return;
        }
            for(Long userId:userIds){
                try{
                    UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
                    if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                        // 账号不存在
                        log.info("fixBrokerSubUser2FaStatus ,userId={} not exist", userId);
                        continue;
                    }
                    User user = this.userMapper.queryByEmail(userIndex.getEmail());
                    if (BitUtils.isFalse(user.getStatus(), Constant.USER_IS_BROKER_SUBUSER)) {
                        log.info("this userId is not brokerSubuser : userId={}", userId);
                        continue;
                    }
                    user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_GOOGLE));
                    userMapper.updateByEmailSelective(user);
                    log.info("update google done : userId={}", userId);
                }catch (Exception e){
                    log.error("fixBrokerSubUser2FaStatus single error-->", e);
                }
            }
	}



}
