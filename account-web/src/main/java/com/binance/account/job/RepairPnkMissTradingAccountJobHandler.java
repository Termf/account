package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.old.data.account.OldUserMapper;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.CreateTradingAccountResponse;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修复pnk表丢失accountid的问题
 * @author pcx
 *
 */
@Log4j2
@JobHandler(value = "repairPnkMissTradingAccountJobHandler")
@Component
public class RepairPnkMissTradingAccountJobHandler extends IJobHandler {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserTradingAccountMapper userTradingAccountMapper;

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
        XxlJobLogger.log("param={0}", param);
        log.info("param={0}", param);
        XxlJobLogger.log("start repairPnkMissTradingAccountJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start repairPnkMissTradingAccountJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isBlank(param)) {
                return SUCCESS;
            }
            repairPnkMissAccountId(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("repairPnkMissTradingAccountJobHandler error-->{0}", e);
            log.error("repairPnkMissTradingAccountJobHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            XxlJobLogger.log("end repairPnkMissTradingAccountJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end repairPnkMissTradingAccountJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
    }

    //注册回写pnk
    public void repairPnkMissAccountId(String userIds) {
        try {
            String[] userIdArr = userIds.split(",");
            List<String> userIdList = Arrays.asList(userIdArr);
            for(String userId:userIdList){
                try{
                    UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId));
                    Long tradingAccountId=userInfoMapper.selectAccountIdByUserId(Long.valueOf(userId));
                    User tempUser;
                    if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())||null==tradingAccountId) {
                        // 账号不存在
                        log.info("repairPnkMissTradingAccountJobHandler.repairPnkMissAccountId ,userId={} not exist",userId);
                        continue;
                    }
                    tempUser = this.userMapper.queryByEmail(userIndex.getEmail());
                    sendAccountActiveMqMsg(tempUser, tradingAccountId);
                }catch (Exception e){
                    log.error("repairPnkMissTradingAccountJobHandler,userid="+userId+":error",e);
                }
            }
        }catch (Exception e) {
            XxlJobLogger.log("repairPnkMissTradingAccountJobHandler error-->{0}", e);
            log.info("repairPnkMissTradingAccountJobHandler error-->{}", e);
        }
    }


    private void sendAccountActiveMqMsg(User tempUser, Long tradingAccount) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, tempUser.getUserId());
        dataMsg.put("tradingAccount", tradingAccount);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_ACTIVE, dataMsg);
        log.info("RepairPnkMissTradingAccountJobHandler.sendAccountActiveMqMsg:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
    }
}
