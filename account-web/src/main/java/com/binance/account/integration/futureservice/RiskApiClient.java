package com.binance.account.integration.futureservice;

import com.binance.future.api.RiskApi;
import com.binance.future.api.request.BatchGetBalanceRisksRequest;
import com.binance.future.api.request.GetBalanceRiskRequest;
import com.binance.future.api.request.GetPositionRiskRequest;
import com.binance.future.api.vo.AccountRiskVO;
import com.binance.future.api.vo.PositionRiskVO;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@Service
public class RiskApiClient {
    @Autowired
    private RiskApi riskApi;

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
        log.info("RiskApiClient.getBalanceRisk start：request={}", request);
        APIResponse<AccountRiskVO> apiResponse = riskApi.getBalanceRisk(APIRequest.instance(originRequest, request));
        log.info("RiskApiClient.getBalanceRisk end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("RiskApiClient.getBalanceRisk : error" + apiResponse.getErrorData());
            throw new BusinessException("getBalanceRisk failed");
        }
        return apiResponse.getData();
    }

    /*
     *
     * batchGetBalanceRisks
     *
     */

    public Map<Long,AccountRiskVO> batchGetBalanceRisks(Set<Long> futureUserIds) throws Exception {
        APIRequest<BatchGetBalanceRisksRequest> originRequest = new APIRequest<BatchGetBalanceRisksRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        BatchGetBalanceRisksRequest request = new BatchGetBalanceRisksRequest();
        request.setFutureUids(futureUserIds);
        log.info("RiskApiClient.batchGetBalanceRisks start：request={}", request);
        APIResponse<Map<Long,AccountRiskVO>> apiResponse = riskApi.batchGetBalanceRisks(APIRequest.instance(originRequest, request));
        log.info("RiskApiClient.batchGetBalanceRisks end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("RiskApiClient.batchGetBalanceRisks : error" + apiResponse.getErrorData());
            throw new BusinessException("batchGetBalanceRisks failed");
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
        log.info("RiskApiClient.getPositionRisk start：request={}", request);
        APIResponse<List<PositionRiskVO>> apiResponse = riskApi.getPositionRisk(APIRequest.instance(originRequest, request));
        log.info("RiskApiClient.getPositionRisk end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("RiskApiClient.getPositionRisk : error" + apiResponse.getErrorData());
            throw new BusinessException("getPositionRisk failed");
        }
        return apiResponse.getData();
    }
}
