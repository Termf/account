package com.binance.account.job;

import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.master.constant.Constant;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 修复  USE_MOBILE状态错误的数据
 *
 * @author pcx
 */
@Log4j2
@JobHandler(value = "repairMistakeStatusForUseMobileJobHandler")
@Component
public class RepairMistakeStatusForUseMobileJobHandler extends IJobHandler {

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
    @Value("${repaireMobileStatusUserList:}")
    private String repairUserList;


    /**
     * userIds:userId集合，用逗号分隔
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        StopWatch sw = new StopWatch();
        XxlJobLogger.log("param={0}", param);
        log.info("param={0}", param);
        XxlJobLogger.log("start repairMistakeStatusForUseMobileJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start repairMistakeStatusForUseMobileJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            repairMistakeUseMobileStatsus(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("repairMistakeStatusForUseMobileJobHandler error-->{0}", e);
            log.error("repairMistakeStatusForUseMobileJobHandler error-->{}", e);
            return FAIL;
        } finally {
            sw.stop();
            XxlJobLogger.log("end repairMistakeStatusForUseMobileJobHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end repairMistakeStatusForUseMobileJobHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
        }
    }

    public void repairMistakeUseMobileStatsus(String userIds) {
        try {
            List<String> userIdList = Lists.newArrayList();
            if (StringUtils.isNotBlank(userIds)) {
                log.info("userIds={}", userIds);
                String[] userIdArr = userIds.split(",");
                userIdList.addAll(Arrays.asList(userIdArr));
            } else if (StringUtils.isNotBlank(repairUserList)) {
                log.info("repairUserList={}", repairUserList);
                String[] repairIdArr = repairUserList.split(",");
                List<String> repairIdList = Arrays.asList(repairIdArr);
                userIdList.addAll(repairIdList);
            }
            for (String userId : userIdList) {
                try {
                    UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
                    if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                        // 账号不存在
                        log.info("repairMistakeStatusForUseMobileJobHandler.repairMistakeUseMobileStatsus ,userId={} not exist", userId);
                        continue;
                    }
                    UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
                    User user = this.userMapper.queryByEmail(userIndex.getEmail());
                    if (StringUtils.isAnyBlank(userSecurity.getMobile(), userSecurity.getMobileCode())) {
                        log.info("mobile or mobilecode anyBlank  : userId={},mobile={},mobilecode={}",
                                userId, userSecurity.getMobile(), userSecurity.getMobileCode());
                        continue;
                    }
                    if (BitUtils.isTrue(user.getStatus(), Constant.USER_MOBILE)) {
                        log.info("USER_MOBILE is true ,so skip it  : userId={}", userId);
                        continue;
                    }
                    UserMobileIndex userMobileIndex = userMobileIndexMapper.selectByPrimaryKey(userSecurity.getMobile(), userSecurity.getMobileCode());
                    if (null == userMobileIndex) {
                        // 记录手机索引
                        final UserMobileIndex mobileIndex = new UserMobileIndex();
                        mobileIndex.setMobile(userSecurity.getMobile());
                        mobileIndex.setCountry(userSecurity.getMobileCode());
                        mobileIndex.setUserId(userIndex.getUserId());
                        this.userMobileIndexMapper.insert(mobileIndex);
                        // 更新user状态
                        final User updateUser = new User();
                        updateUser.setEmail(userIndex.getEmail());
                        updateUser.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_MOBILE));
                        this.userMapper.updateUserStatusByEmail(updateUser);
                        log.info("fix mobileIndex and status userId={}", userId);
                    } else {
                        if (userMobileIndex.getUserId().longValue() != user.getUserId().longValue()) {
                            log.info("userMobileIndex userId={},inputUserId={} unequal ", userMobileIndex.getUserId(), userId);
                            continue;
                        }
                        // 更新user状态
                        final User updateUser = new User();
                        updateUser.setEmail(userIndex.getEmail());
                        updateUser.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_MOBILE));
                        this.userMapper.updateUserStatusByEmail(updateUser);
                        log.info("fix status userId={}", userId);
                    }
                } catch (Exception e) {
                    log.error("repairMistakeStatusForUseMobileJobHandler,userid=" + userId + ":error", e);
                }
            }
        } catch (Exception e) {
            XxlJobLogger.log("repairMistakeStatusForUseMobileJobHandler error-->{0}", e);
            log.info("repairMistakeStatusForUseMobileJobHandler error-->{}", e);
        }
    }


}
