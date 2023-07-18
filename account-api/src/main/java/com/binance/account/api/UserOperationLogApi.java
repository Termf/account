package com.binance.account.api;

import com.binance.account.vo.operationlog.UserOperationLogVo;
import com.binance.account.vo.security.request.*;
import com.binance.account.vo.security.response.UserOperationLogResultResponse;
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

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/userOperationLog")
@Api(value = "用户行为日志")
public interface UserOperationLogApi {

    @ApiOperation("查询用户行为日志")
    @PostMapping("/query")
    APIResponse<UserOperationLogResultResponse> listUserOperationLogs(
            @RequestBody @Validated APIRequest<UserOperationLogRequest> request) throws Exception;

    @ApiOperation("查询用户行为日志(User View)")
    @PostMapping("/query/user-view")
    APIResponse<UserOperationLogResultResponse> listUserOperationLogsUserView(
            @RequestBody @Validated APIRequest<UserOperationLogUserViewRequest> request) throws Exception;

    @ApiOperation("查询今天相同operation和ip，不同userId的UserOperationLog")
    @PostMapping("/queryToday")
    APIResponse<UserOperationLogResultResponse> findTodaysUserOperationLogs(
            @RequestBody @Validated APIRequest<FindTodaysUserOperationLogsRequest> request) throws Exception;

    @ApiOperation("count今天UserOperationLog")
    @PostMapping("/countToday")
    APIResponse<Long> countTodaysUserOperationLogs(
            @RequestBody @Validated APIRequest<CountTodaysUserOperationLogsRequest> request) throws Exception;

    @ApiOperation("一段时间内的登陆成功的次数, count(distinct userId)")
    @PostMapping("/countLogin")
    APIResponse<Long> countDistinctLogin(
            @RequestBody @Validated APIRequest<CountLoginRequest> request) throws Exception;

    @ApiOperation("查询单条用户行为日志")
    @PostMapping("/queryDetail") APIResponse<UserOperationLogVo> queryDetail(
            @RequestBody @Validated APIRequest<UserIdAndIdRequest> request) throws Exception;

    @ApiOperation("查询单条用户行为日志")
    @PostMapping("/queryDetailWithUuid")
    APIResponse<UserOperationLogVo> queryDetailWithUuid(
            @RequestBody @Validated APIRequest<QueryDetailWithUuidRequest> request) throws Exception;
}