package com.binance.account.service.user;

import com.binance.account.vo.apiagentreward.request.ApiAgentRewardQuery;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.request.IfNewUserRequest;
import com.binance.account.vo.apiagentreward.request.SelectApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.ApiAgentRewardAdminVo;
import com.binance.account.vo.apiagentreward.response.IfNewUserResponse;
import com.binance.account.vo.apiagentreward.response.SelectApiAgentRewardResponse;
import com.binance.master.commons.SearchResult;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * @author zhao chenkai
 * @date 2020/01/16
 */
public interface IApiAgentReward {

    /**
     * 根据用户id和Api返佣码查用户Api返佣比例
     * @param request
     * @return
     */
    APIResponse<SelectApiAgentRewardResponse> selectApiAgentReward(APIRequest<SelectApiAgentRewardRequest> request);

    APIResponse<IfNewUserResponse> ifNewUser(APIRequest<IfNewUserRequest> request);

    SearchResult<ApiAgentRewardAdminVo> queryApiAgentReward(ApiAgentRewardQuery query);

    void addApiAgentReward(ApiAgentRewardRequest request);

    ApiAgentRewardAdminVo apiAgentRewardInfo(Long id);

    void updateApiAgentReward(ApiAgentRewardRequest request);

    void deleteApiAgentReward(ApiAgentRewardRequest request);
}
