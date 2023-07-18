package com.binance.account.integration.fiat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.c2c.api.FiatUserApi;
import com.binance.c2c.vo.user.request.CreateFiatUserReq;
import com.binance.margin.api.bookkeeper.request.CreateMarginAccountRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FiatAccountApiClient {

    @Autowired
    private FiatUserApi fiatUserApi;


    public void newFiatAccount(CreateFiatUserReq request) throws Exception {
        APIRequest<CreateMarginAccountRequest> originRequest = new APIRequest<CreateMarginAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        APIResponse<Integer> apiResponse = fiatUserApi.createFiatUser(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAccountApiClient.newMarginAccount :rootUserId=" + request.getUserId() + "  error"
                    + apiResponse.getErrorData());
            throw new BusinessException("newMarginAccount failed");
        }
    }
}
