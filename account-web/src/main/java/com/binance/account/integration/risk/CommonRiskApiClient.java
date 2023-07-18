package com.binance.account.integration.risk;

import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.rule.api.CommonRiskApi;
import com.binance.rule.request.DecisionCommonRequest;
import com.binance.rule.response.DecisionCommonResponse;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Log4j2
@Service
public class CommonRiskApiClient {
    @Resource
    private CommonRiskApi commonRiskApi;


    public DecisionCommonResponse commonRule(Map<String, Object> context,String eventCode){
        DecisionCommonRequest request = new DecisionCommonRequest();
        request.setEventCode(eventCode);
        request.setContext(context);
        log.info("CommonRiskApiClient.commonRule eventCode:{}, context:{}", eventCode, JsonUtils.toJsonNotNullKey(context));
        APIResponse<DecisionCommonResponse> apiResponse = commonRiskApi.commonRule(APIRequest.instance(request));
        log.info("CommonRiskApiClient.commonRule reault:{}",JsonUtils.toJsonNotNullKey(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("CommonRiskApiClient.commonRule :"+ "  error" + apiResponse.getErrorData());
            throw new BusinessException("call commonRule failed");
        }
        return apiResponse.getData();
    }


    public DecisionCommonResponse commonRuleForSubTransferToParent(String subUserId,Double amount,String asset,String parentUserId){
        Map<String, Object> context = Maps.newHashMap();
        context.put("uid", subUserId);
        context.put("amount", amount);
        context.put("asset", asset);
        context.put("toUid", parentUserId);
        context.put("source", "SUB_TRANSFER_TO_PARENT");
        return commonRule(context,"sub_transfer_to_parent");
    }




}
