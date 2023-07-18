package com.binance.account.controller.apiagentreward;

import com.binance.account.service.user.IApiAgentReward;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardQuery;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.ApiAgentRewardAdminVo;
import com.binance.master.commons.SearchResult;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.ApiAgentRewardAdminApi;

/**
 * @author zhao chenkai
 * @date 2020/01/16
 */
@RestController
public class ApiAgentRewardAdminController implements ApiAgentRewardAdminApi {

    @Autowired
    private IApiAgentReward apiAgentReward;

    @Override
    public APIResponse<SearchResult<ApiAgentRewardAdminVo>> queryApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardQuery> request) {
        SearchResult<ApiAgentRewardAdminVo> pageList = apiAgentReward.queryApiAgentReward(request.getBody());
        return APIResponse.getOKJsonResult(pageList);
    }

    @Override
    public APIResponse<Void> addApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardRequest> request) {
        apiAgentReward.addApiAgentReward(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<ApiAgentRewardAdminVo> apiAgentRewardInfo(@RequestBody @Validated APIRequest<Long> request) {
        ApiAgentRewardAdminVo ApiAgentRewardAdminVo = apiAgentReward.apiAgentRewardInfo(request.getBody());
        return APIResponse.getOKJsonResult(ApiAgentRewardAdminVo);
    }

    @Override
    public APIResponse<Void> updateApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardRequest> request) {
        apiAgentReward.updateApiAgentReward(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> deleteApiAgentReward(@RequestBody APIRequest<ApiAgentRewardRequest> request) {
        apiAgentReward.deleteApiAgentReward(request.getBody());
        return APIResponse.getOKJsonResult();
    }
}
