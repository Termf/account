package com.binance.account.service.notification.impl;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.binance.push.api.IPushApi;
import com.binance.push.vo.request.SinglePushRequest;
import com.binance.push.vo.request.enums.MsgTypeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Log4j2
@Service
public class PushCenterService {
    private static final String PUSH_PREFIX_SINGLE = "ACCOUNT:SEC";

    private final EnumMap<SecurityNotificationEnum, String> scenarioMap = new EnumMap<>(SecurityNotificationEnum.class);

    @Value("${account.is.use.new.push.center}")
    private boolean isUseNewPushCenter;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${local-ip}")
    private String serviceLocalIp;

    @Value("${account.new.push.center.white.list}")
    private Set<Long> pushCenterWhiteList;

    @Resource
    private IPushApi iPushApi;

    @PostConstruct
    private void init() {
        scenarioMap.put(SecurityNotificationEnum.DEVICE_AUTH, "security-device-alert");
        scenarioMap.put(SecurityNotificationEnum.RESET_2FA, "security-reset-2fa");
        scenarioMap.put(SecurityNotificationEnum.FORGET_PWD, "security-forget-password");
    }

    public boolean isUseNewApi(Long userId) {
        boolean isUseNewPush = isUseNewPushCenter || pushCenterWhiteList.contains(userId);
        log.info("current switch result is {} userId={}", isUseNewPush, userId);
        return isUseNewPush;
    }

    public void saveSecurityNotification(Long userId, SecurityNotificationEnum securityNotiEum, LanguageEnum langEnum) {
        try {
            SinglePushRequest singlePushRequest = buildSinglePushRequest(userId, securityNotiEum, langEnum);
            APIResponse response = iPushApi.asyncSinglePush(APIRequest.instance(singlePushRequest));
            log.info("call push center single async push result is= {} userId={} securityNotiEum={}, lang={}", response, userId, securityNotiEum, langEnum);
        } catch (Exception e) {
            log.error("call push center asyncSinglePush error", e);
        }
    }

    private SinglePushRequest buildSinglePushRequest(Long userId, SecurityNotificationEnum securityNotiEum, LanguageEnum langEnum) {
        SinglePushRequest singlePushRequest = new SinglePushRequest();
        String scenario = scenarioMap.get(securityNotiEum);
        singlePushRequest.setLanguage(langEnum.getCode());
        singlePushRequest.setBizScenarioCode(scenario);
        singlePushRequest.setMsgType(MsgTypeEnum.APP);
        singlePushRequest.setSource(serviceName + "-" + serviceLocalIp);
        singlePushRequest.setPushId(PUSH_PREFIX_SINGLE + UUID.randomUUID().toString());
        singlePushRequest.setUserId(String.valueOf(userId));
        singlePushRequest.setParams(Collections.emptyMap());
        return singlePushRequest;
    }
}
