package com.binance.account.integration.futureservice;

//import com.binance.delivery.periphery.api.DeliveryUserSettingApi;
//import com.binance.delivery.periphery.api.request.core.NewAccountReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DeliveryUserSettingApiClient {
//    @Autowired
//    private DeliveryUserSettingApi deliveryUserSettingApi;


    /*
     * 同步母子账号期货交割账号的费率
     */

    public void syncNewSubAccount(Long subUserFutureUserId, Long parentFutureUserId) throws Exception {
//        APIRequest<NewAccountReq> originRequest = new APIRequest<NewAccountReq>();
//        originRequest.setLanguage(LanguageEnum.ZH_CN);
//        originRequest.setTerminal(TerminalEnum.WEB);
//        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
//        NewAccountReq request = new NewAccountReq();
//        request.setSubFutureUid(subUserFutureUserId);
//        request.setSuperFutureUid(parentFutureUserId);
//        log.info("DeliveryUserSettingApiClient.syncNewSubAccount start：request={}", request);
//        APIResponse apiResponse = deliveryUserSettingApi.syncNewSubAccount(APIRequest.instance(originRequest, request));
//        log.info("DeliveryUserSettingApiClient.syncNewSubAccount end ：request={},response={}", request, apiResponse);
//        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
//            log.error("DeliveryUserSettingApiClient.syncNewSubAccount : error" + apiResponse.getErrorData());
//            throw new BusinessException("syncNewSubAccount failed");
//        }
    }
}
