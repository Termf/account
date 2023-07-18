package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.security.UserSecurityMapper;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MQ消息丢失回写pnk
 *
 * @author mengjuan
 */
@Log4j2
@JobHandler(value = "fixMissSecurityLevelHandler")
@Component
public class FixMissSecurityLevelHandler extends IJobHandler {

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
        XxlJobLogger.log("start fixMissSecurityLevelHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start fixMissSecurityLevelHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if (StringUtils.isBlank(param)) {
                return SUCCESS;
            }
            mqToPnk(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("fixMissSecurityLevelHandler error-->{0}", e);
            log.error("fixMissSecurityLevelHandler error-->{}", e);
            return FAIL;
        } finally {
            sw.stop();
            XxlJobLogger.log("end fixMissSecurityLevelHandler,endTimeStamp={0}", sw.getTotalTimeSeconds());
            log.info("end fixMissSecurityLevelHandler,endTimeStamp={}", sw.getTotalTimeSeconds());

        }
    }

    private void mqToPnk(String param) {
        String[] userIdArr = param.split(",");
        List<String> userIdList = Arrays.asList(userIdArr);
        for (String userId : userIdList) {
            repairSecurityLevel(userId);
        }

    }

    /**
     * 修改用户等级
     */
    private void repairSecurityLevel(String userId) {
        try {
            UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
            if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                // 账号不存在
                log.info("fixMissSecurityLevelHandler.repairSecurityLevel ,userId={} not exist", userId);
                return;
            }
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(Long.parseLong(userId.trim()));
            User user = this.userMapper.queryByEmail(userIndex.getEmail());
            if (Objects.isNull(userSecurity) && Objects.isNull(user) && Objects.nonNull(userSecurity.getSecurityLevel())) {
                log.info("user or userSecurity or userSecurity.getSecurityLevel nonNull  : userId={}", userId);
                return;
            }
            Integer securityLevel = userSecurity.getSecurityLevel() == null ? 1 : userSecurity.getSecurityLevel();
            userSecurity.setSecurityLevel(securityLevel);
            userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
            Map<String, Object> dataMsg = new HashMap<>();
            dataMsg.put("userId", userIndex.getUserId());
            dataMsg.put("level", securityLevel);
            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.SECURITY_LEVEL, dataMsg);
            this.iMsgNotification.send(msg);
            log.info("iMsgNotification.repairSecurityLevel: {}", JSON.toJSONString(msg));
        } catch (Exception e) {
            XxlJobLogger.log("repairSecurityLevel error,userId:{0},error:{1}", userId, e);
            log.warn("repairSecurityLevel error,userId:{},error:{}", userId, e);
        }
    }


}
