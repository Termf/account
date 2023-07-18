package com.binance.account.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.LogMaskUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

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
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * 一键注册用户激活回写pnk
 *
 */
@Log4j2
@JobHandler(value = "oneButtonUserActiveToPnkHandler")
@Component
public class OneButtonUserActiveToPnkHandler extends IJobHandler {

	private static final ExecutorService oneButtonUserActiveToPnkExecutor = Executors.newFixedThreadPool(10);


	@Resource
    private UserInfoMapper userInfoMapper;
	
	@Resource
    private UserMapper userMapper;
	
	@Resource
    private UserIndexMapper userIndexMapper;
	
	@Resource
    private IMsgNotification iMsgNotification;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch sw = new StopWatch();
        XxlJobLogger.log("start oneButtonUserActiveToPnkHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start oneButtonUserActiveToPnkHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            oneButtonUserActiveToPnkJob();
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("oneButtonUserActiveToPnkHandler error-->{0}", e);
            log.error("oneButtonUserActiveToPnkHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            XxlJobLogger.log("end oneButtonUserToPnkHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end oneButtonUserToPnkHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
	}
	
	//注册回写pnk
	public void oneButtonUserActiveToPnkJob() {
		try {
			List<Long> userIdList = userMapper.selectAllOneButtonUser();
			log.info("oneButtonUserActiveToPnkHandler 一键注册用户总数为:{}", userIdList.size());
			for (Long userId : userIdList) {
				oneButtonUserActiveToPnkExecutor.execute(new Runnable() {
					@Override
					public void run() {
						log.info("oneButtonUserActiveToPnkHandler 开始处理userId={}", userId);
						try {
							UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
							User user = userMapper.queryByExistentEmail(userIndex.getEmail());
							if (BitUtils.isFalse(user.getStatus(), AccountCommonConstant.ONE_BUTTON_REGISTER_USER)) {
								return;
							}
							UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
							// 生成了tradingAccount的，才同步
							if (userInfo == null || userInfo.getTradingAccount() == null) {
								return;
							}

							Map<String, Object> dataMsg = new HashMap<>();
							dataMsg.put(UserConst.USER_ID, user.getUserId());
							dataMsg.put("tradingAccount", userInfo.getTradingAccount());
							MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_ACTIVE, dataMsg);
							log.info("oneButtonUserActiveToPnkHandler sendMsg:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(dataMsg)));
							iMsgNotification.send(msg);
							
							log.info("oneButtonUserActiveToPnkHandler 处理成功 userId={}", userId);
						} catch (Exception e) {
							log.error("oneButtonUserActiveToPnkHandler 处理失败 userId=" + userId, e);
						}
					}
				});
			}
		}catch (Exception e) {
			XxlJobLogger.log("oneButtonUserActiveToPnkHandler error-->{0}", e);
	         log.info("oneButtonUserActiveToPnkHandler error-->{}", e);
		}
	}

}
