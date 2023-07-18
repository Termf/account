package com.binance.account.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by Fei.Huang on 2019/1/14.
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/withdraw/policy")
@Api(value = "用户提现额度规则")
public interface WithdrawPolicyApi {

    @ApiOperation("获取日提现额度列表")
    @GetMapping("/daily/limits")
    APIResponse<List<BigDecimal>> getDailyWithdrawLimits() throws Exception;

    @ApiOperation("根据用户SecurityLevel获取日提现额度")
    @PostMapping("/daily/limit")
    APIResponse<BigDecimal> getDailyWithdrawLimitBySecurityLevel(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户SecurityLevel获取ReviewQuota")
    @PostMapping("/review-quota")
    APIResponse<BigDecimal> getWithdrawReviewQuota(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

}
