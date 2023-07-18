package com.binance.account.api;

import com.binance.account.vo.security.request.UserIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/waas")
@Api(value = "waas相关api")
public interface UserWaasApi {

    @ApiOperation("将userid设置为waas账户(wallet as a service)")
    @PostMapping("/setToWaasAccount")
    APIResponse<Void> setToWaasAccount(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

}
