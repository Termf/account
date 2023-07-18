package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.constants.enums.AccountSysTypeEnum;
import com.binance.account.domain.bo.AccountMsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.user.IUserSimpleBusiness;
import com.binance.master.utils.LogMaskUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  该类存放非mapper、client类普通调用
 */
@Log4j2
@Service
public class UserSimpleBusiness implements IUserSimpleBusiness {

    @Resource
    protected IMsgNotification iMsgNotification;

    public void sendMgsToFrontGroup(String routingKey, String userId, String eventType,String accountType,String tfaType){
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, String.valueOf(userId));
        dataMsg.put("eventTime", new Date().getTime());
        dataMsg.put("eventType",eventType);
        dataMsg.put("accountType",accountType);
        dataMsg.put("tfaType",tfaType);
        AccountMsgNotification msg = new AccountMsgNotification(routingKey,  dataMsg);
        log.info("iMsgNotification userId:{},future register:{}",userId, LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
    }

}
