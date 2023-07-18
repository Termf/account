package com.binance.account.job;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.enums.SysType;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * 注册回写pnk(account注册成功，但rabbitmq挂了，导致pnk没没同步，可以用改job修复)
 * @author mengjuan
 *
 */
@Log4j2
@JobHandler(value = "registUserToPnkHandler")
@Component
public class RegistUserToPnkHandler extends IJobHandler {
	
	@Resource
    private UserInfoMapper userInfoMapper;
	
	@Resource
    private UserMapper userMapper;
	
	@Resource
    private UserIndexMapper userIndexMapper;
	
	@Resource
    private IMsgNotification iMsgNotification;
	/**
	 * userIds:userId集合，用逗号分隔
	 */
	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch sw = new StopWatch();
        log.info("param={}", param);
        XxlJobLogger.log("start registUserToPnkHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start registUserToPnkHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isBlank(param)) {
            	return SUCCESS;
    		}
            registUserToPnkJob(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("registUserToPnkHandler error-->{0}", e);
            log.error("registUserToPnkHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            XxlJobLogger.log("end registUserToPnkHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end registUserToPnkHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	//注册回写pnk
	public void registUserToPnkJob(String userIds) {
		try {
			String[] userIdArr = userIds.split(",");
			List<String> userIdList = Arrays.asList(userIdArr);
			userIdList.forEach(userId->{
				UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId));
				User user = userMapper.queryByExistentEmail(userIndex.getEmail());
				UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
				
				if(userIndex == null || user == null || userInfo == null) {
					 XxlJobLogger.log("userIndex or user or userInfo is null,{0},{1},{2}", JSON.toJSON(userIndex), JSON.toJSON(user), JSON.toJSON(userInfo));
			         log.info("userIndex or user or userInfo is null,{},{},{}", JSON.toJSON(userIndex), JSON.toJSON(user), JSON.toJSON(userInfo));
				}else {
					Map<String, Object> dataMsg = new HashMap<>();
			        dataMsg.put(UserConst.USER_ID, user.getUserId());
			        dataMsg.put(UserConst.EMAIL, userIndex.getEmail());
			        dataMsg.put("salt", user.getSalt());
			        dataMsg.put("password", user.getPassword());
			        dataMsg.put("registerToken", "");
			        dataMsg.put("code", "");
			        dataMsg.put("agentId", userInfo.getAgentId());
			        dataMsg.put("trackSource", userInfo.getTrackSource());
			        dataMsg.put("ipAddress", "");
			        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.REGISTER, dataMsg);
			        log.info("iMsgNotification Job register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
			        this.iMsgNotification.send(msg);
				}
			});
		}catch (Exception e) {
			XxlJobLogger.log("registUserToPnkJob error-->{0}", e);
	         log.info("registUserToPnkJob error-->{}", e);
		}
	}

}
