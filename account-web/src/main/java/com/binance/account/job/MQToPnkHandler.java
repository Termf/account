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
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.enums.SysType;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * MQ消息丢失回写pnk
 * @author mengjuan
 *
 */
@Log4j2
@JobHandler(value = "mQToPnkHandler")
@Component
public class MQToPnkHandler extends IJobHandler {

	@Resource
    private UserInfoMapper userInfoMapper;
	
	@Resource
    private UserMapper userMapper;
	
	@Resource
    private UserIndexMapper userIndexMapper;
	
	@Resource
    private IMsgNotification iMsgNotification;
	
	@Resource
	private UserSecurityMapper userSecurityMapper;
	
	/**
	 * param:userId集合，用逗号分隔
	 */
	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch sw = new StopWatch();
        log.info("param={}", param);
        XxlJobLogger.log("start MQToPnkHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start MQToPnkHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isBlank(param)) {
            	return SUCCESS;
    		}
			mqToPnk(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("MQToPnkHandler error-->{0}", e);
            log.error("MQToPnkHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            XxlJobLogger.log("end MQToPnkHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end MQToPnkHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	private void mqToPnk(String param) {
		String[] userIdArr = param.split(",");
		List<String> userIdList = Arrays.asList(userIdArr);
		userIdList.forEach(userId->{
			UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId));
			if(userIndex != null) {
				bindMobileJob(userIndex);//绑定手机
				//unbindMobileJob(userIndex);//解绑手机
				//绑定谷歌、解绑谷歌都不在修改user_security表的encryptedSecretKey
				//bindGoogleJob(userIndex);//绑定谷歌
				//unbindGoogleJob(userIndex);//解绑谷歌
				//updatePwd(userIndex);//修改密码
				securityLevel(userIndex);//修改用户等级
				antiPhishingCode(userIndex);//修改防钓鱼码
			}
			
		});
		
	}

	/**
	 * 修改防钓鱼码
	 * @param userIndex
	 */
	private void antiPhishingCode(UserIndex userIndex) {
		try{
		 	Map<String, Object> dataMsg = new HashMap<String, Object>();
            dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
            UserSecurity us = userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
		    if(us != null) {
	            dataMsg.put("antiPhishingCode", us.getAntiPhishingCode());
	            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ANTI_PHISHING_CODE, dataMsg);
	            log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
	            this.iMsgNotification.send(msg);
	        }
		}catch (Exception e) {
			XxlJobLogger.log("antiPhishingCode error,userId:{0},error:{1}",userIndex.getUserId(), e);
			log.warn("antiPhishingCode error,userId:{},error:{}",userIndex.getUserId(), e);
		}
		
	}



	/**
	 * 修改密码(account 和 pnk 不一致才改)
	 * @param userIndex
	 */
	/*private void updatePwd(UserIndex userIndex) {
		try {
			Map<String, Object> dataMsg = new HashMap<>();
	        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
	        dataMsg.put(UserConst.EMAIL, userIndex.getEmail());
	        User user = userMapper.queryByEmail(userIndex.getEmail());
	        if(user != null) {
		        dataMsg.put("salt", user.getSalt());
		        dataMsg.put("password", user.getPassword());
		        dataMsg.put("disableToken", "");
		        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UPDATE_PWD, dataMsg);
		        log.info("iMsgNotification updatePwd:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
		        this.iMsgNotification.send(msg);
	        }
		}catch(Exception e) {
		 	XxlJobLogger.log("updatePwd error,userId:{0},error:{1}",userIndex.getUserId(), e);
			log.warn("updatePwd error,userId:{},error:{}",userIndex.getUserId(), e);
		}
	}*/

	/**
	 * 修改用户等级
	 * @param userIndex
	 */
	private void securityLevel(UserIndex userIndex) {
		try {
		    Map<String, Object> dataMsg = new HashMap<>();
		    dataMsg.put("userId", userIndex.getUserId());
		    UserSecurity us = userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
		    if(us != null) {
		        dataMsg.put("level", us.getSecurityLevel());
		        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
		        this.iMsgNotification.send(msg);
		        log.info("security level compensate send mq: {}", JSON.toJSONString(msg));
		    }
		}catch(Exception e) {
		 	XxlJobLogger.log("updatePwd error,userId:{0},error:{1}",userIndex.getUserId(), e);
			log.warn("updatePwd error,userId:{},error:{}",userIndex.getUserId(), e);
		}
	}

	/**
	 * 绑定谷歌
	 * @param userIndex
	 */
	private void bindGoogleJob(UserIndex userIndex) {
		try {
			Map<String, Object> dataMsg = new HashMap<String, Object>();
	        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
	        User user = userMapper.queryByEmail(userIndex.getEmail());
			UserStatusEx status = new UserStatusEx(user.getStatus());
			if(!status.getIsUserGoogle()) {//未绑定状态的要绑定谷歌
		        dataMsg.put("secretKey", "");
		        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_GOOGLE, dataMsg);
		        log.info("iMsgNotification bindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
			}
		}catch(Exception e) {
			XxlJobLogger.log("bindGoogleJob error,userId:{0},error:{1}",userIndex.getUserId(), e);
			log.warn("bindGoogleJob error,userId:{},error:{}",userIndex.getUserId(), e);
		}
	}
	
	/**
	 * 解绑谷歌
	 * @param userIndex
	 */
	private void unbindGoogleJob(UserIndex userIndex) {
		Map<String, Object> dataMsg = new HashMap<String, Object>();
	    dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
	    User user = userMapper.queryByEmail(userIndex.getEmail());
	    UserStatusEx status = new UserStatusEx(user.getStatus());
	    if(status.getIsUserGoogle()) {//绑定了谷歌验证的要解绑
	        dataMsg.put("disableToken", "");//emailVerifyCode
		    MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_GOOGLE_VERIFY, dataMsg);
		    log.info("iMsgNotification unbindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
		    this.iMsgNotification.send(msg);
	    }
	}
	

	/**
	 * 绑定手机号
	 * @param userIdList
	 */
	private void bindMobileJob(UserIndex userIndex) {
		try {
			 Map<String, Object> dataMsg = new HashMap<String, Object>();
		     dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
		     UserSecurity us = userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
		     if(us == null || (StringUtils.isBlank(us.getMobile()) && StringUtils.isBlank(us.getMobileCode()))) {//未绑定手机号的要绑定
		    	 dataMsg.put("mobile", us.getMobile());
			     dataMsg.put("mobileCode", us.getMobileCode());
			     MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_MOBILE, dataMsg);
			     log.info("iMsgNotification bindMobile:{}", JSON.toJSONString(msg));
			     this.iMsgNotification.send(msg);	        
		     }
		}catch(Exception e) {
			XxlJobLogger.log("bindMobileJob error,userId:{0},error:{1}",userIndex.getUserId(), e);
			log.warn("bindMobileJob error,userId:{},error:{}",userIndex.getUserId(), e);
		}
	}
	
	/**
	 * 解绑手机
	 * @param userIndex
	 */
	/*private void unbindMobileJob(UserIndex userIndex) {
		try {
			Map<String, Object> dataMsg = new HashMap<>();
	        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
	        UserSecurity us = userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
		    if(us != null && (StringUtils.isNotBlank(us.getMobile()) || StringUtils.isNotBlank(us.getMobileCode()))) {//绑定了手机号的要解绑
		        dataMsg.put("disableToken", "");//emailVerifyCode
		        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_MOBILE, dataMsg);
		        log.info("iMsgNotification unbindMobile:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
		        this.iMsgNotification.send(msg);
		    }
		}catch(Exception e) {
			log.warn("unbindMobileJob error,userId:{},error:{}",userIndex.getUserId(), e);
		}
	}*/


}
