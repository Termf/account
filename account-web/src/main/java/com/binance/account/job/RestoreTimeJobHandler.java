package com.binance.account.job;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.data.entity.user.UserAgentReward;
import com.binance.account.data.mapper.user.UserAgentRewardMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.master.utils.DateUtils;
import com.google.gson.Gson;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * 根据恢复时间恢复返佣比例
 * @author mengjuan
 *
 */
@Log4j2
@JobHandler(value = "restoreTimeJobHandler")
@Component
public class RestoreTimeJobHandler extends IJobHandler{
	
	@Resource
    private UserAgentRewardMapper userAgentRewardMapper;
	
	@Resource
    private UserInfoMapper userInfoMapper;
    
	@Override
	public ReturnT<String> execute(String param) throws Exception {
	 StopWatch sw = new StopWatch();
     XxlJobLogger.log("start restoreTimeJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
     log.info("start restoreTimeJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
     try {
         sw.start();
         //查询待修改数据
         List<UserAgentReward> userIdList = userAgentRewardMapper.selectIsRestoreInfo();
         //update userAgentReward
         userAgentRewardMapper.updateExpectTime();
         //update userInfo
         Gson json = new Gson();
         log.info(json.toJson(userIdList));
         for(UserAgentReward agent : userIdList) {
        	 userInfoMapper.updateUserInfoAgentReward(agent);
         }
         return SUCCESS;
     } catch (Exception e) {
         XxlJobLogger.log("restoreTimeJobHandler error-->{0}", e);
         log.error("restoreTimeJobHandler error-->{}", e);
         return FAIL;
     }finally {
         sw.stop();
         XxlJobLogger.log("end restoreTimeJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
         log.info("end restoreTimeJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
         
     }
	}

}