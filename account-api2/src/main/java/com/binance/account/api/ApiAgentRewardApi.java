package com.binance.account.api;

import com.binance.account.vo.apiagentreward.request.IfNewUserRequest;
import com.binance.account.vo.apiagentreward.response.IfNewUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.apiagentreward.request.SelectApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.SelectApiAgentRewardResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author zhao chenkai
 * @date 2020/01/16
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/apiAgentReward")
@Api(value = "现货api返佣")
public interface ApiAgentRewardApi {

    @ApiOperation("根据用户id和Api返佣码查用户Api返佣比例")
    @PostMapping("/selectApiAgentReward")
    APIResponse<SelectApiAgentRewardResponse> selectApiAgentReward(@Validated @RequestBody APIRequest<SelectApiAgentRewardRequest> request) throws Exception;

    @ApiOperation("根据用户id和Api返佣码查询用户是否满足返佣条件")
    @PostMapping("/ifNewUser")
    APIResponse<IfNewUserResponse> ifNewUser(@Validated @RequestBody APIRequest<IfNewUserRequest> request) throws Exception;

}
