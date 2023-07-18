package com.binance.account.job;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.binance.master.old.data.account.OldUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.enums.SysType;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.DateUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.CreateTradingAccountResponse;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;

import lombok.extern.log4j.Log4j2;

/**
 * 账户自检修复job
 * @author mengjuan
 *
 */
@Log4j2
@JobHandler(value = "repairTradingAccountJobHandler")
@Component
public class RepairTradingAccountJobHandler extends IJobHandler {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserTradingAccountMapper userTradingAccountMapper;

    @Resource
    private AccountApi accountApi;

    @Resource
    protected ISysConfig iSysConfig;

    @Resource
    private IMsgNotification iMsgNotification;

    @Resource
    private OldUserMapper oldUserMapper;

    private static final BigDecimal BASIS_POINT = new BigDecimal("10000");

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        StopWatch sw = new StopWatch();
        XxlJobLogger.log("start repairTradingAccountJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start repairTradingAccountJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            tradingcountJob();
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("repairTradingAccountJobHandler error-->{0}", e);
            log.error("repairTradingAccountJobHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            XxlJobLogger.log("end repairTradingAccountJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end repairTradingAccountJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
    }


    /**
     * 账户自检修复(10分钟执行一次) 1.job account; 2.通过mq同步数据到pnk
     */
    public void tradingcountJob() {
        List<UserInfo> users = userInfoMapper.getEmptyAccount();

        if (!users.isEmpty()) {
            for (UserInfo userInfo : users) {
                Long userId = userInfo.getUserId();
                log.info("需修复用户-->{}", userId);
                XxlJobLogger.log("需修复用户-->{0}", userId);
                try {
                    // 调用撮合引擎v1/accountByExternalId接口
                    List<TradingAccountDetails> result = accountApi.accountByExternalId(userId);
                    if (!result.isEmpty() && result.get(0) != null && result.get(0).getAccountId() != null) {
                        // 修改、同步数据
                        operateAccountId(result.get(0).getAccountId(), userId);
                    } else {
                        boolean canTrade = true;
                        if (null != this.iSysConfig.selectByDisplayName("user_activate")
                                && this.iSysConfig.selectByDisplayName("user_activate").getCode().equals("1")) {
                            canTrade = false;
                        }
                        // 调用撮合接口获取accountId
                        CreateTradingAccountResponse response = accountApi.getTradingAccountId(userId,
                                userInfo.getMakerCommission().multiply(BASIS_POINT).longValue(),
                                userInfo.getTakerCommission().multiply(BASIS_POINT).longValue(),
                                userInfo.getBuyerCommission().multiply(BASIS_POINT).longValue(),
                                userInfo.getSellerCommission().multiply(BASIS_POINT).longValue(), canTrade);


                        if (response != null && response.getAccountId() != null) {
                            // 修改、同步数据
                            operateAccountId(response.getAccountId(), userId);
                        }

                    }
                } catch (Exception e) {
                    XxlJobLogger.log("tradingcountJob error,userId-->{0};error is-->{1}", userId, e);
                    log.error("tradingcountJob error,userId-->{};error is-->{}", userId, e);
                }
            }
        }

        try {
	        //查pnk的accountId为null且是距当前时间的前1h-前0.5h的数据
	        List<String> userIdList = oldUserMapper.getEmptyPnkTradingAccount();
	        
	        //遍历account的数据，account的accountId为不为null的情况则是需要修复的数据，循环发mq到pnk 补偿数据
	        userIdList.stream().forEach(strUserId->{
	        	Long userId = Long.valueOf(strUserId);
	        	Long tradingAccount = userInfoMapper.selectAccountIdByUserId(userId);
				if(null != tradingAccount) {
					log.info("pnk需修复用户-->{}", strUserId);
		            XxlJobLogger.log("pnk需修复用户-->{0}", strUserId);
					//发mq修复pnk的tradingAccount
					updatePnkBySendMq(tradingAccount, userId);
				}
	        });
        }catch (Exception e) {
			log.error("修复pnk的tradingAccount出错-->", e);
		}

    }

    /**
     * 修改account数据，数据修改同步到pnk
     * @param accountId
     * @param userId
     */
    private void operateAccountId(Long accountId, Long userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setTradingAccount(accountId);
        userInfo.setUserId(userId);
        // 1.update user_info表 AccountId;
        userInfoMapper.updateByPrimaryKeySelective(userInfo);

        // 2.往user_trading_account表insert数据
        UserTradingAccount userTradingAccount = new UserTradingAccount();
        userTradingAccount.setTradingAccount(accountId);
        userTradingAccount.setUserId(userId);
        userTradingAccountMapper.insertIgnore(userTradingAccount);
        
        // 3.同步修改pnk
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put("userId", userId);
        dataMsg.put("accountId", accountId);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.TRADING_ACCOUNT, dataMsg);
        log.info("iMsgNotification trading_account:{}", JSON.toJSONString(msg));
        XxlJobLogger.log("iMsgNotification trading_account:{0}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        log.info("用户完成账号自检修复:{}", userId);
        XxlJobLogger.log("{0}用户完成账号自检修复", userId);
    }


    /**
     * 发mq到pnk修复pnk的tradingAccount数据
     * @param accountId:tradingAccount
     * @param userId
     */
	private void updatePnkBySendMq(Long accountId, Long userId) {
		Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put("userId", userId);
        dataMsg.put("tradingAccount", accountId);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_ACTIVE, dataMsg);
        log.info("iMsgNotification trading_account:{}", JSON.toJSONString(msg));
        XxlJobLogger.log("iMsgNotification trading_account:{0}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        log.info("用户完成账号自检修复:{}", userId);
        XxlJobLogger.log("{0}用户完成账号自检修复", userId);
	}

}
