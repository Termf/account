package com.binance.account.service.async;

import com.binance.account.aop.SecurityLog;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import lombok.extern.log4j.Log4j2;
import org.javasimon.aop.Monitored;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Fei.Huang on 2018/10/15.
 */
@Log4j2
@Component
public class SecurityLogAsyncTask {

    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;

    @Monitored
    @Async("simpleRequestAsync")
    public void saveSecurityLog(Long userId, String ip, SecurityLog securityLog, APIRequestHeader header) {
        try {
            final UserSecurityLog log = new UserSecurityLog();
            log.setUserId(userId);
            log.setIp(ip);
            if (header != null) {
                log.setClientType(header.getTerminal().getCode());
            } else {
                log.setClientType(TerminalEnum.OTHER.getCode());
            }
            log.setIpLocation(IP2LocationUtils.getCountryCity(ip));
            log.setOperateType(securityLog.operateType());
            log.setDescription(securityLog.name());
            log.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(log);
        } catch (Exception e) {
            log.error(String.format("saveSecurityLog failed, userId:%s, exception:", userId), e);
        }
    }
}