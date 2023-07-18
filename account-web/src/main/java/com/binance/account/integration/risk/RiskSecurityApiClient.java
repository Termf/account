package com.binance.account.integration.risk;

import com.alibaba.fastjson.JSON;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.risk.api.RiskSecurityApi;
import com.binance.risk.vo.CheckUserRiskRequestVo;
import com.binance.risk.vo.RiskSecurityVo;
import com.binance.risk.vo.UserIdRequestVo;
import com.binance.rule.api.CommonRiskApi;
import com.binance.rule.request.DecisionCommonRequest;
import com.binance.rule.response.DecisionCommonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Log4j2
@Service
public class RiskSecurityApiClient {

    @Autowired
    private RiskSecurityApi riskSecurityApi;

    @Resource
    private CommonRiskApi commonRiskApi;


    public Boolean checkUserRisk(Long userId,CheckUserRiskRequestVo.RiskScenario scenario){
        APIRequest<CheckUserRiskRequestVo> originRequest = new APIRequest<CheckUserRiskRequestVo>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        CheckUserRiskRequestVo request = new CheckUserRiskRequestVo();
        request.setUserId(userId);
        request.setScenario(scenario);

        APIResponse<Boolean> apiResponse = riskSecurityApi.checkUserRisk(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("RiskSecurityApiClient.checkUserRisk :userId=" + userId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("RiskSecurityApiClient failed");
        }
        return apiResponse.getData();
    }

    public RiskSecurityVo getBanCaseByUserId(Long userId){
        log.info("RiskSecurityApiClient.getBanCaseByUserId.userId:{}",userId);
        APIRequest<UserIdRequestVo> request = new APIRequest<>();
        UserIdRequestVo vo = new UserIdRequestVo();
        vo.setUserId(userId);
        request.setBody(vo);
        APIResponse<RiskSecurityVo> response = riskSecurityApi.getBanCaseByUserId(request);
        log.info("RiskSecurityApiClient.getBanCaseByUserId.response:{}",JsonUtils.toJsonHasNullKey(response));
        if (APIResponse.Status.ERROR == response.getStatus()) {
            throw new BusinessException("RiskSecurityApiClient failed");
        }
        return response.getData();
    }

    public DecisionCommonResponse commonRule(DecisionCommonRequest request) {
        APIResponse<DecisionCommonResponse> response = commonRiskApi.commonRule(APIRequest.instance(request));
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("RiskSecurityApiClient.commonRule,request={}", JSON.toJSONString(request));
            throw new BusinessException("RiskSecurityApiClient failed");
        }
        log.info("RiskSecurityApiClient.commonRule,request={},response={}", JSON.toJSONString(request), JSON.toJSONString(response));
        return response.getData();
    }

}
