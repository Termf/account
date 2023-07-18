package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 修复子账号回写pnk库费率丢失的问题
 *
 * @author pcx
 */
@Log4j2
@JobHandler(value = "repairSubAccountMissCommissionJobHandler")
@Component
public class RepairSubAccountMissCommissionJobHandler extends IJobHandler {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private UserMobileIndexMapper userMobileIndexMapper;
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
        XxlJobLogger.log("start repairSubAccountMissCommissionJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start repairSubAccountMissCommissionJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            String[] requestparam = param.split("_");
            String model=requestparam[0];
            if(requestparam.length==2){
                String userIdList=requestparam[1];
                repairSubAccountMissCommission(model,userIdList);
            }else{
                repairSubAccountMissCommission(model,null);
            }
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("repairSubAccountMissCommissionJobHandler error-->{0}", e);
            log.error("repairSubAccountMissCommissionJobHandler error-->{}", e);
            return FAIL;
        } finally {
            sw.stop();
            XxlJobLogger.log("end repairSubAccountMissCommissionJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end repairSubAccountMissCommissionJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
    }

    public void repairSubAccountMissCommission(String model,String userIds) {
        try {
            List<String> userIdList = Lists.newArrayList();
            if (StringUtils.isNotBlank(userIds)) {
                log.info("userIds={}", userIds);
                String[] userIdArr = userIds.split(",");
                userIdList.addAll(Arrays.asList(userIdArr));
            }
            if("ALL".equalsIgnoreCase(model)){
                List<User> userList = userMapper.getAllSubAccount();
                List<String> dbUserList = Lists.transform(userList, new Function<User, String>() {
                    @Override
                    public String apply(@Nullable User user) {
                        return user.getUserId().toString();
                    }
                });
                userIdList.addAll(dbUserList);
            }

            if("HOUR".equalsIgnoreCase(model)){
                List<User> userList = userMapper.getAllSubAccountLastOneHour();
                List<String> dbUserList = Lists.transform(userList, new Function<User, String>() {
                    @Override
                    public String apply(@Nullable User user) {
                        return user.getUserId().toString();
                    }
                });
                userIdList.addAll(dbUserList);
            }

            for (String userId : userIdList) {
                try {
                    UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
                    if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                        // 账号不存在
                        log.info("repairSubAccountMissCommissionJobHandler.repairSubAccountMissCommission ,userId={} not exist", userId);
                        continue;
                    }
                    User user = this.userMapper.queryByEmail(userIndex.getEmail());
                    if (BitUtils.isFalse(user.getStatus(), Constant.USER_IS_SUBUSER)) {
                        log.info("this userId is not subuser : userId={}", userId);
                        continue;
                    }
                    UserInfo subUserInfo =userInfoMapper.selectByPrimaryKey(user.getUserId());
                    // 同步数据至PNK
                    try {
                        Map<String, Object> dataMsg = Maps.newHashMap();
                        dataMsg.put(UserConst.USER_ID, user.getUserId());
                        dataMsg.put("buyerCommission", subUserInfo.getBuyerCommission());
                        dataMsg.put("sellerCommission", subUserInfo.getSellerCommission());
                        dataMsg.put("takerCommission", subUserInfo.getTakerCommission());
                        dataMsg.put("makerCommission", subUserInfo.getMakerCommission());
                        dataMsg.put("modifyReason", "子母账户绑定");
                        dataMsg.put("expectedRestoreTime", "");
                        MsgNotification msg =
                                new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.SET_COMMISSION, dataMsg);
                        log.info("repaireSubAccountiMsgNotification setCommission:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
                        this.iMsgNotification.send(msg);
                    } catch (Exception e) {
                        log.error("repaireSubAccountiMsgNotification.send failed:", e);
                    }
                } catch (Exception e) {
                    log.error("repairSubAccountMissCommissionJobHandler,userid=" + userId + ":error", e);
                }
            }
        } catch (Exception e) {
            XxlJobLogger.log("repairSubAccountMissCommissionJobHandler error-->{0}", e);
            log.info("repairSubAccountMissCommissionJobHandler error-->{}", e);
        }
    }


}
