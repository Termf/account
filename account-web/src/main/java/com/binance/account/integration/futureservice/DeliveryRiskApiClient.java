package com.binance.account.integration.futureservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.delivery.periphery.api.DeliveryRiskApi;
import com.binance.delivery.periphery.api.request.core.GetBalanceRiskRequest;
import com.binance.delivery.periphery.api.request.core.GetPositionRiskRequest;
import com.binance.delivery.periphery.api.vo.core.AccountRiskVO;
import com.binance.delivery.periphery.api.vo.core.PositionRiskVO;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DeliveryRiskApiClient {
    @Autowired
    private DeliveryRiskApi deliveryRiskApi;

    /*
     *
     * getBalanceRisk
     *
     */

    public AccountRiskVO getBalanceRisk(Long futureAccountId) throws Exception {
        APIRequest<GetBalanceRiskRequest> originRequest = new APIRequest<GetBalanceRiskRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetBalanceRiskRequest request = new GetBalanceRiskRequest();
        request.setAccountId(futureAccountId);
        log.info("DeliveryRiskApiClient.getBalanceRisk start：request={}", request);
        APIResponse<AccountRiskVO> apiResponse = deliveryRiskApi.getBalanceRisk(APIRequest.instance(originRequest, request));
        log.info("DeliveryRiskApiClient.getBalanceRisk end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("DeliveryRiskApiClient.getBalanceRisk : error" + apiResponse.getErrorData());
            throw new BusinessException("getBalanceRisk failed");
        }
        return apiResponse.getData();
    }


    public List<PositionRiskVO> getPositionRisk(Long futureAccountId) throws Exception {
        APIRequest<GetPositionRiskRequest> originRequest = new APIRequest<GetPositionRiskRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetPositionRiskRequest request = new GetPositionRiskRequest();
        request.setAccountId(futureAccountId);
        log.info("DeliveryRiskApiClient.getPositionRisk start：request={}", request);
        APIResponse<List<PositionRiskVO>> apiResponse = deliveryRiskApi.getPositionRisk(APIRequest.instance(originRequest, request));
        log.info("DeliveryRiskApiClient.getPositionRisk end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("DeliveryRiskApiClient.getPositionRisk : error" + apiResponse.getErrorData());
            throw new BusinessException("getPositionRisk failed");
        }
        return apiResponse.getData();
    }
}
