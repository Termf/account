package com.binance.account.controller.certificate;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserRiskRatingApi;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserRiskRatingQuery;
import com.binance.account.service.certificate.IUseRiskRating;
import com.binance.account.vo.user.UserRiskRatingVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@RestController
public class UserRiskRatingController implements UserRiskRatingApi {
    @Resource
    private IUseRiskRating riskRating;

    @Override
    public APIResponse<SearchResult<UserRiskRatingVo>> getList(@RequestBody() @Validated APIRequest<UserRiskRatingQuery> request)
            throws Exception {
        return riskRating.getList(request);
    }


    
}
