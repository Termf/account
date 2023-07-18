package com.binance.account.integration.featureservice;

import com.binance.featureservice.api.IFeatureValueApi;
import com.binance.featureservice.vo.feature.FeatureRequest;
import com.binance.featureservice.vo.feature.FeatureResponse;
import com.binance.future.api.request.GetBalanceRiskRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Log4j2
@Service
public class FeatureValueApiApiClient {
    @Autowired
    private IFeatureValueApi featureValueApi;

    /*
     *
     * getFeature
     *
     */

    public FeatureResponse getFeature(String variable,Map<String, String> parameters) throws Exception {
        APIRequest<GetBalanceRiskRequest> originRequest = new APIRequest<GetBalanceRiskRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        FeatureRequest request = new FeatureRequest();
        request.setVariable(variable);
        request.setParameters(parameters);
        log.info("FeatureValueApiApiClient.getFeature start：request={}", request);
        APIResponse<FeatureResponse> apiResponse = featureValueApi.getFeature(APIRequest.instance(originRequest, request));
        log.info("FeatureValueApiApiClient.getFeature end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("FeatureValueApiApiClient.getFeature : error" + apiResponse.getErrorData());
            throw new BusinessException("getFeature failed");
        }
        return apiResponse.getData();
    }



    /*
     *
     * getOtcLoanLockBtc
     *
     */

    public BigDecimal getOtcLoanLockBtc(Long userId) throws Exception {
        BigDecimal result=BigDecimal.ZERO;
        String variable="user_locked_collateral_in_btc";
        Map<String, String> parameters = Maps.newHashMap();
        try{
            parameters.put("uid",userId.toString());
            FeatureResponse featureResponse= getFeature(variable,parameters);
            result=result.add(new BigDecimal(featureResponse.getVariable().getValue()));
        }catch (Exception e){
           log.error("getOtcLoanLockBtc error=",e);
        }
        return result;
    }

}
