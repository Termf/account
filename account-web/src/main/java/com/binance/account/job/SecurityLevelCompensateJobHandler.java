package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.enums.SysType;
import com.binance.master.old.data.account.OldUserDataMapper;
import com.binance.master.old.models.account.OldUserData;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lufei
 * @date 2018/10/23
 */
@Log4j2
@JobHandler(value = "securityLevelCompensateJobHandler")
@Component
public class SecurityLevelCompensateJobHandler extends IJobHandler {

    @Resource
    private IMsgNotification iMsgNotification;

    @Resource
    private UserSecurityMapper userSecurityMapper;

    @Resource
    private OldUserDataMapper oldUserDataMapper;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("security level compensate job start");
        XxlJobLogger.log("security level compensate job start");
        try {
            List<Long> userIds = this.lookCompensateUserId();
            List<List<Long>> partitions = Lists.partition(userIds, 500);
            for (List<Long> partition : partitions) {
                Map<Long, Integer> pnkMap = this.pnkUserSecurityLevel(partition);
                for (Long userId : partition) {
                    this.sendSecurityLevelMq(userId, pnkMap.get(userId));
                }
            }
            log.info("security level compensate job success");
            XxlJobLogger.log("security level compensate job success");
            return SUCCESS;
        } catch (Exception e) {
            log.error("security level compensate job send mq exception: ", e);
            XxlJobLogger.log("security level compensate job send mq exception: {0}", e);
            return FAIL;
        } finally {
            sw.stop();
            log.info("security level compensate job takes {}s", sw.getTotalTimeSeconds());
            XxlJobLogger.log("security level compensate job takes {0}s", sw.getTotalTimeSeconds());
        }
    }

    private List<Long> lookCompensateUserId() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        Date recentTime = calendar.getTime();
        log.info("security level compensate job recent time: {}", recentTime);
        return this.userSecurityMapper.selectRecentUpdateUserId(recentTime);
    }

    private void sendSecurityLevelMq(Long userId, Integer pnkSecurityLevel) {
        if (pnkSecurityLevel == null) {
            log.info("security level compensate find pnk security level is null, userId: {}", userId);
            return;
        }
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null || userSecurity.getSecurityLevel() == null) {
            log.info("security level compensate find account security level is null, userId: {}", userId);
            return;
        }
        if (userSecurity.getSecurityLevel().compareTo(pnkSecurityLevel) == 0) {
            log.info("security level compensate find account security level {}, pnk {}, userId: {}", userSecurity.getSecurityLevel(), pnkSecurityLevel, userId);
            return;
        }
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put("userId", userSecurity.getUserId());
        dataMsg.put("level", userSecurity.getSecurityLevel());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
        this.iMsgNotification.send(msg);
        log.info("security level compensate send mq: {}", JSON.toJSONString(msg));
    }

    private Map<Long, Integer> pnkUserSecurityLevel(List<Long> userIds) {
        List<OldUserData> list =
                oldUserDataMapper.selectByUserIds(userIds.stream().map(u -> u + "").collect(Collectors.toList()));
        return list.stream().collect(Collectors.toMap(k -> Long.parseLong(k.getUserId()), v -> v.getSecurityLevel()));
    }
}
