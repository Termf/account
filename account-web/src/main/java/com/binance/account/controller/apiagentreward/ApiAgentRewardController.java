package com.binance.account.controller.apiagentreward;

import com.binance.account.vo.apiagentreward.request.IfNewUserRequest;
import com.binance.account.vo.apiagentreward.response.IfNewUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.ApiAgentRewardApi;
import com.binance.account.service.user.IApiAgentReward;
import com.binance.account.vo.apiagentreward.request.SelectApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.SelectApiAgentRewardResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * @author zhao chenkai
 * @date 2020/01/16
 */
@RestController
public class ApiAgentRewardController implements ApiAgentRewardApi {

    @Autowired
    private IApiAgentReward apiAgentReward;

    @Override
    public APIResponse<SelectApiAgentRewardResponse> selectApiAgentReward(@RequestBody @Validated APIRequest<SelectApiAgentRewardRequest> request) throws Exception {
        return apiAgentReward.selectApiAgentReward(request);
    }

    @Override
    public APIResponse<IfNewUserResponse> ifNewUser(@RequestBody @Validated APIRequest<IfNewUserRequest> request) {
        return apiAgentReward.ifNewUser(request);
    }
}
