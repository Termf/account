package com.binance.account.utils;

import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.models.APIRequest;

import java.util.Map;

/**
 * Created by yangyang on 2019/9/25.
 */
public class InboxUtils {

    public static APIRequest<PushInboxMessage> getPushInboxMessageAPIRequest(Long userId, Map<String, Object> data, String lang, String terminalCode, String templateCode) {
        APIRequest<PushInboxMessage> apiRequest = new APIRequest<>();
        PushInboxMessage pushInboxMessage = new PushInboxMessage();
        pushInboxMessage.setLanguage(lang);
        pushInboxMessage.setParam(data);
        pushInboxMessage.setSendClient(terminalCode);
        pushInboxMessage.setUserId(userId);
        pushInboxMessage.setTemplateCode(templateCode);
        apiRequest.setBody(pushInboxMessage);
        return apiRequest;
    }
}
