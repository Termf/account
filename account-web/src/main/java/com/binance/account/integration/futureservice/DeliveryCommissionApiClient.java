package com.binance.account.integration.futureservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.delivery.periphery.api.DeliveryCommissionApi;
import com.binance.delivery.periphery.api.request.core.GetCommissionRequest;
import com.binance.delivery.periphery.api.request.core.InitUserRequest;
import com.binance.delivery.periphery.api.vo.core.UserTierVO;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DeliveryCommissionApiClient {
    @Autowired
    private DeliveryCommissionApi deliveryCommissionApi;


    /*
     *
     * 初始化用户信息
     *
     */

    public void initUser(Long rootUserId,Long futureUserId, Long futureAccountId,String email, Long refferalFutureUserId) throws Exception {
        APIRequest<InitUserRequest> originRequest = new APIRequest<InitUserRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        InitUserRequest request = new InitUserRequest();
        request.setUid(rootUserId);
        request.setFutureUid(futureUserId);
        request.setAccountId(futureAccountId);
        request.setEmail(email);
        request.setReferralFutureUid(refferalFutureUserId);
        log.info("DeliveryCommissionApi.initUser start：request={}", request);
        APIResponse apiResponse = deliveryCommissionApi.initUser(APIRequest.instance(originRequest, request));
        log.info("DeliveryCommissionApi.initUser end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("DeliveryCommissionApi.initUser : error" + apiResponse.getErrorData());
            throw new BusinessException("initUser failed");
        }
    }

    /*
     *
     * getCommission
     *
     */

    public UserTierVO getCommission(Long futureUserId) throws Exception {
        APIRequest<GetCommissionRequest> originRequest = new APIRequest<GetCommissionRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetCommissionRequest request = new GetCommissionRequest();
        request.setFutureUid(futureUserId);
        log.info("DeliveryCommissionApi.getCommission start：request={}", request);
        APIResponse<UserTierVO> apiResponse = deliveryCommissionApi.getCommission(APIRequest.instance(originRequest, request));
        log.info("DeliveryCommissionApi.getCommission end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("DeliveryCommissionApi.getCommission : error" + apiResponse.getErrorData());
            throw new BusinessException("getCommission failed");
        }
        return apiResponse.getData();
    }
}
