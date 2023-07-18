package com.binance.account.integration.message;

import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.messaging.api.msg.UserLanguageApi;
import com.binance.messaging.api.msg.request.user.language.UserLanguageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class UserLanguageApiClient {
    @Autowired
    private UserLanguageApi userLanguageApi;


    public void saveOrUpdate(String userId,String language) {
        UserLanguageRequest requestBody = new UserLanguageRequest();
        requestBody.setUserId(userId);
        requestBody.setLanguage(language);
        APIResponse<Boolean> resp = null;
        try {
            log.info("UserLanguageApiClient.saveOrUpdate request:{}", JsonUtils.toJsonNotNullKey(requestBody));
            resp = userLanguageApi.saveOrUpdate(APIRequest.instance(requestBody));
            log.info("UserLanguageApiClient.saveOrUpdate Resp:{}", JsonUtils.toJsonNotNullKey(resp));
            if (resp.getStatus() == APIResponse.Status.ERROR) {
                log.error("UserLanguageApiClient.saveOrUpdate : error" + resp.getErrorData());
                throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
            }
        } catch (Exception e) {
            log.error("UserLanguageApiClient.saveOrUpdate failed,", e);
        }
    }
}
