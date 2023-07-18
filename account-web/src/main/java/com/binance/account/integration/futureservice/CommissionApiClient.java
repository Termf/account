package com.binance.account.integration.futureservice;

import com.binance.future.api.CommissionApi;
import com.binance.future.api.request.GetCommissionRequest;
import com.binance.future.api.request.InitUserRequest;
import com.binance.future.api.request.UpdateUserReferralAgentRequest;
import com.binance.future.api.vo.UserTierVO;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CommissionApiClient {
    @Autowired
    private CommissionApi commissionApi;


    /*
     *
     * 初始化用户信息
     *
     */

    public void initUser(Long rootUserId,Long futureUserId, Long futureAccountId,String email, Long refferalFutureUserId, Long parentFutureUserId) throws Exception {
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
        request.setParentBrokerFutureUid(parentFutureUserId);
        log.info("ComissionApiClient.initUser start：request={}", request);
        APIResponse apiResponse = commissionApi.initUser(APIRequest.instance(originRequest, request));
        log.info("CommissionApiClient.initUser end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("CommissionApiClient.initUser : error" + apiResponse.getErrorData());
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
        log.info("CommissionApiClient.getCommission start：request={}", request);
        APIResponse<UserTierVO> apiResponse = commissionApi.getCommission(APIRequest.instance(originRequest, request));
        log.info("CommissionApiClient.getCommission end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("CommissionApiClient.getCommission : error" + apiResponse.getErrorData());
            throw new BusinessException("getCommission failed");
        }
        return apiResponse.getData();
    }

    public boolean updateFutureAgent(Long futureUserId, Long futureAgentId) {
        APIRequest<UpdateUserReferralAgentRequest> originRequest = new APIRequest<UpdateUserReferralAgentRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        UpdateUserReferralAgentRequest request = new UpdateUserReferralAgentRequest();
        request.setFutureUid(futureUserId);
        request.setAgentFutureUid(futureAgentId);
        log.info("CommissionApiClient.updateFutureAgent start：request={}", request);
        APIResponse<Boolean> apiResponse = commissionApi.updateUserReferralAgent(APIRequest.instance(originRequest, request));
        log.info("CommissionApiClient.updateFutureAgent end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("CommissionApiClient.updateFutureAgent : error" + apiResponse.getErrorData());
            throw new BusinessException("updateFutureAgent failed");
        }
        return apiResponse.getData();
    }
}
