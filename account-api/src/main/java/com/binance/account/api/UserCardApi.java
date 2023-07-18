package com.binance.account.api;

import com.binance.account.vo.card.request.CreateCardAccountRequest;
import com.binance.account.vo.card.response.CreateCardAccountResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/card")
@Api(value = "card相关api")
public interface UserCardApi {

    @ApiOperation("根据userid创建card账户")
    @PostMapping("/createCardAccount")
    APIResponse<CreateCardAccountResponse> createCardAccount(@RequestBody APIRequest<CreateCardAccountRequest> request)
            throws Exception;

}
