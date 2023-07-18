package com.binance.account.api;

import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.FutureUserAgentResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/future")
@Api(value = "future相关api")
public interface UserFutureApi {

    @ApiOperation("根据userid创建future账户")
    @PostMapping("/createFutureAccount")
    APIResponse<CreateFutureUserResponse> createFutureAccount(@RequestBody APIRequest<CreateFutureAccountRequest> request)
            throws Exception;

    @ApiOperation("根据FutureTradingAccountId获取FutureuserId")
    @PostMapping("/getFutureUserIdByFutureTradingAccount")
    APIResponse<Long> getFutureUserIdByFutureTradingAccount(@RequestBody APIRequest<GetUserIdByTradingAccountRequest> request)
            throws Exception;

    @ApiOperation("根据FutureDeliveryTradingAccountId获取FutureuserId")
    @PostMapping("/getFutureUserIdByFutureDeliveryTradingAccount")
    APIResponse<Long> getFutureUserIdByFutureDeliveryTradingAccount(@RequestBody APIRequest<GetUserIdByTradingAccountRequest> request)
            throws Exception;

    @ApiOperation("判断是否开通期货账户")
    @PostMapping("/checkIfOpenFutureAccount")
    APIResponse<Boolean> checkIfOpenFutureAccount(@RequestBody APIRequest<CheckIfOpenFutureAccountRequest> request)
            throws Exception;

    @ApiOperation("发送期货平仓或者减仓信息")
    @PostMapping("/sendFutureClosePositionMsg")
    APIResponse<Boolean> sendFutureClosePositionMsg(@RequestBody APIRequest<SendFutureClosePositionMsgRequest> request)
            throws Exception;

    @ApiOperation("发送期货保证金")
    @PostMapping("/sendFutureMarginCall")
    APIResponse<Boolean> sendFutureMarginCall(@RequestBody APIRequest<SendFutureMarginCallRequest> request)
            throws Exception;

    @ApiOperation("创建期货账户推荐2码")
    @PostMapping("/createFutureUserAgent")
    public APIResponse<String> createFutureUserAgent(@RequestBody @Validated APIRequest<FutureUserAgentReq> request)
            throws Exception;

    @ApiOperation("查询future返佣码")
    @PostMapping("/selectFutureUserAgent")
    public APIResponse<FutureUserAgentResponse>  selectFutureUserAgent(@RequestBody @Validated APIRequest<CheckFutureAgentCodeExistReq> request)
            throws Exception;

    @ApiOperation("发送期货资金费率信息")
    @PostMapping("/sendFutureFundingRateMsg")
    APIResponse<Boolean> sendFutureFundingRateMsg(@RequestBody @Validated APIRequest<SendFutureFundingRateMsgRequest> request)
            throws Exception;

    @ApiOperation("根据futureUserIds查询agentCode-特殊使用,key=futureUserId,value=agentcode")
    @PostMapping("/selectFutureAgentCodes")
    APIResponse<Map<Long,String>> selectFutureAgentCodes(@RequestBody @Validated APIRequest<List<Long>> request)
            throws Exception;

    @ApiOperation("发送各种call")
    @PostMapping("/sendFutureCall")
    APIResponse<Boolean> sendFutureCall(@RequestBody  @Validated APIRequest<SendFutureCallRequest> request)
            throws Exception;

    @ApiOperation("根据rootUserId判断是否可以使用future推荐码")
    @PostMapping("/checkIfCanUseFutreAgentCode")
    APIResponse<Boolean> checkIfCanUseFutreAgentCode(@RequestBody @Validated APIRequest<Long> request)
            throws Exception;

    @ApiOperation("查询rootUser的future delivery账户信息，如果没有则新建deliveryAccount")
    @PostMapping("/createDeliveryAccountIfNotExist")
    APIResponse<CreateFutureUserResponse> createDeliveryAccountIfNotExist(@RequestBody APIRequest<IdRequest> request)
            throws Exception;

    @ApiOperation("同步永续账号的apikey到交割账户")
    @PostMapping("/syncApiKeyToDelivery")
    APIResponse<Void> syncApiKeyToDelivery(@RequestBody APIRequest<IdRequest> request)
            throws Exception;



}
