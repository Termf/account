package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
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
 * 修复  USE_MOBILE状态错误的数据
 *
 * @author pcx
 */
@Log4j2
@JobHandler(value = "repairMissBindMobileJobHandler")
@Component
public class RepairMissBindMobileJobHandler extends IJobHandler {

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
        XxlJobLogger.log("start repairMissBindMobileJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start repairMissBindMobileJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            repairMissBindMobile(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("repairMissBindMobileJobHandler error-->{0}", e);
            log.error("repairMissBindMobileJobHandler error-->{}", e);
            return FAIL;
        } finally {
            sw.stop();
            XxlJobLogger.log("end repairMissBindMobileJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end repairMissBindMobileJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
    }

    public void repairMissBindMobile(String userIds) {
        try {
            List<String> userIdList = Lists.newArrayList();
            if (StringUtils.isNotBlank(userIds)) {
                log.info("userIds={}", userIds);
                String[] userIdArr = userIds.split(",");
                userIdList.addAll(Arrays.asList(userIdArr));
            } else {
                List<User> userList = userMapper.selectBindMobileUserList();
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
                        log.info("repairMissBindMobileJobHandler.repairMistakeUseMobileStatsus ,userId={} not exist", userId);
                        continue;
                    }
                    UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
                    User user = this.userMapper.queryByEmail(userIndex.getEmail());
                    if (BitUtils.isFalse(user.getStatus(), Constant.USER_MOBILE)) {
                        log.info("USER_MOBILE is false ,so skip it  : userId={}", userId);
                        continue;
                    }
                    if (StringUtils.isAnyBlank(userSecurity.getMobile(), userSecurity.getMobileCode())) {
                        log.info("mobile or mobilecode anyBlank  : userId={},mobile={},mobilecode={}",
                                userId, userSecurity.getMobile(), userSecurity.getMobileCode());
                    }
                    // 绑定手机消息通知 start
                    Map<String, Object> dataMsg = Maps.newHashMap();
                    dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
                    dataMsg.put("mobile", userSecurity.getMobile());
                    dataMsg.put("mobileCode", userSecurity.getMobileCode());
                    MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.BIND_MOBILE, dataMsg);
                    log.info("iMsgNotification repairMissBindMobile:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
                    this.iMsgNotification.send(msg);
                } catch (Exception e) {
                    log.error("repairMistakeStatusForUseMobileJobHandler,userid=" + userId + ":error", e);
                }
            }
        } catch (Exception e) {
            XxlJobLogger.log("repairMissBindMobileJobHandler error-->{0}", e);
            log.info("repairMissBindMobileJobHandler error-->{}", e);
        }
    }


}
