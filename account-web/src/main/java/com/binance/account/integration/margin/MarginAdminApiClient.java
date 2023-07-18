package com.binance.account.integration.margin;

import com.binance.margin.api.admin.MarginAdminApi;
import com.binance.margin.api.admin.response.MarginTradeCoeffResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MarginAdminApiClient {
    @Autowired
    private MarginAdminApi marginAdminApi;


    public MarginTradeCoeffResponse marginTradeCoeff(Long rootUserId){
        log.info("MarginAdminApiClient.marginTradeCoeff start：rootUserId={}", rootUserId);
        APIResponse<MarginTradeCoeffResponse> apiResponse = marginAdminApi.marginTradeCoeff(rootUserId);
        log.info("MarginAdminApiClient.marginTradeCoeff end ：rootUserId={},response={}", rootUserId, JsonUtils.toJsonNotNullKey(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAdminApiClient.marginTradeCoeff :rootUserId=" + rootUserId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("marginTradeCoeff failed");
        }
        return apiResponse.getData();
    }

}
