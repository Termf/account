package com.binance.account.api;

import com.binance.account.vo.mining.request.CreateMingAccountRequest;
import com.binance.account.vo.mining.response.CreateMiningUserResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/mining")
@Api(value = "mining相关api")
public interface UserMiningApi {

    @ApiOperation("根据userid创建矿池账户")
    @PostMapping("/createMiningAccount")
    APIResponse<CreateMiningUserResponse> createMiningAccount(@RequestBody APIRequest<CreateMingAccountRequest> request)
            throws Exception;

}
