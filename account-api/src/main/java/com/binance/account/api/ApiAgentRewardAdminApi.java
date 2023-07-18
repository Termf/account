package com.binance.account.api;

import com.binance.account.vo.apiagentreward.request.ApiAgentRewardQuery;
import com.binance.account.vo.apiagentreward.request.ApiAgentRewardRequest;
import com.binance.account.vo.apiagentreward.response.ApiAgentRewardAdminVo;
import com.binance.master.commons.SearchResult;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;

import io.swagger.annotations.Api;

/**
 * @author zhao chenkai
 * @date 2020/01/16
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/apiAgentReward/admin")
@Api(value = "api现货返佣后台接口")
public interface ApiAgentRewardAdminApi {

    @ApiOperation("分页查询")
    @PostMapping("/queryApiAgentReward")
    APIResponse<SearchResult<ApiAgentRewardAdminVo>> queryApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardQuery> request);

    @ApiOperation("添加")
    @PostMapping("/addApiAgentReward")
    APIResponse<Void> addApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardRequest> request);

    @ApiOperation("查询单个信息")
    @PostMapping("/apiAgentRewardInfo")
    APIResponse<ApiAgentRewardAdminVo> apiAgentRewardInfo(@RequestBody @Validated APIRequest<Long> request);

    @ApiOperation("修改")
    @PostMapping("/updateApiAgentReward")
    APIResponse<Void> updateApiAgentReward(@RequestBody @Validated APIRequest<ApiAgentRewardRequest> request);

    @ApiOperation("删除")
    @PostMapping("/deleteApiAgentReward")
    APIResponse<Void> deleteApiAgentReward(@RequestBody APIRequest<ApiAgentRewardRequest> request);
}
