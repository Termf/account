package com.binance.account.api;

import com.binance.account.vo.withdraw.request.WithdrawAddressCheckRequest;
import com.binance.account.vo.withdraw.response.WithdrawAddressCheckResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhenleisun
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/capitalInOutCheck")
@Api(value = "用户提现地址安全检测")
public interface CapitalInOutCheckApi {
    /**
     * 用于检查用户的提现地址是否安全。
     *
     * @param request
     * @return
     */
    @ApiOperation("提现地址风险检测。返回为空表示未检测到风险。")
    @PostMapping("/address")
    APIResponse<WithdrawAddressCheckResponse> checkWithdrawAddress(@Validated @RequestBody APIRequest<WithdrawAddressCheckRequest> request);
}
