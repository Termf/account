package com.binance.account.service.datamigration.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.service.datamigration.IMsgNotification;
import com.google.common.collect.Maps;

import lombok.extern.log4j.Log4j2;

/**
 * @author Men Huatao (alex.men@binance.com)
 * @date 2020/8/20
 */
@Log4j2
@Service
public class MsgNotificationToC2CHelper {
    @Autowired
    private IMsgNotification iMsgNotification;

    private static final String EXCHANGE = "c2c.message.notify.exchange";
    private static final String ROUTING_KEY_ACCOUNT = "c2c.user.open.account";
    private static final String ROUTING_KEY_CHANGE = "c2c.user.info.changed";

    public void sendActiveAccountMsgAsync(UserInfo userInfo, UserInfo fiatUserInfo, UserSecurity userSecurity) {
        log.info("start sendActiveAccountMsg");
        AsyncTaskExecutor.execute(() -> {
            Map<String, Object> msg = Maps.newHashMap();
            msg.put("userId", userInfo.getUserId());
            msg.put("tradingAccount", userInfo.getTradingAccount());
            msg.put("fiatUserId", fiatUserInfo.getUserId());
            msg.put("fiatTradingAccount", fiatUserInfo.getTradingAccount());
            msg.put("email", userSecurity.getEmail());
            msg.put("mobile", userSecurity.getMobile());
            msg.put("parentId", userInfo.getParent());
            msg.put("registrationTime", userInfo.getInsertTime());
            iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_ACCOUNT, msg);
        });
    }

    public void sendMobileBindingChangeMsgAsync(UserSecurity userSecurity) {
        log.info("start sendMobileBindingChangeMsg");
        AsyncTaskExecutor.execute(() -> {
            Map<String, Object> msg = Maps.newHashMap();
            msg.put("userId", userSecurity.getUserId());
            msg.put("mobile", userSecurity.getMobile());
            iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_CHANGE, msg);
        });
    }

    public void sendEmailBindingChangeMsgAsync(UserSecurity userSecurity) {
        log.info("start sendEmailBindingChangeMsg");
        AsyncTaskExecutor.execute(() -> {
            Map<String, Object> msg = Maps.newHashMap();
            msg.put("userId", userSecurity.getUserId());
            msg.put("email", userSecurity.getEmail());
            iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_CHANGE, msg);
        });
    }

    public void sendNickNameChangeMsgAsync(UserInfo userInfo) {
        log.info("start sendEmailBindingChangeMsg");
        AsyncTaskExecutor.execute(() -> {
            Map<String, Object> msg = Maps.newHashMap();
            msg.put("userId", userInfo.getUserId());
            msg.put("nickName", userInfo.getNickName());
            iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_CHANGE, msg);
        });
    }

    /**
     * 子、母账号类型变化信息同步
     */
    public void sendUserTypeChangesMsgAsync(Long parentUserId, List<Long> subUserIds, boolean isBinding) {
        log.info("start sendEmailBindingChangeMsg");
        AsyncTaskExecutor.execute(() -> {
            if (Objects.nonNull(parentUserId)) {
                Map<String, Object> parentMsg = Maps.newHashMap();
                parentMsg.put("userId", parentUserId);
                parentMsg.put("isParent", isBinding);
                iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_CHANGE, parentMsg);
            }

            if (CollectionUtils.isNotEmpty(subUserIds)) {
                subUserIds.forEach(subUserId -> {
                    Map<String, Object> subUserMsg = Maps.newHashMap();
                    subUserMsg.put("userId", subUserId);
                    subUserMsg.put("isSubUser", isBinding);
                    iMsgNotification.sendNotification(EXCHANGE, ROUTING_KEY_CHANGE, subUserMsg);
                });
            }
        });
    }
}
