package com.binance.account.controller.operationlog;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.api.UserOperationLogApi;
import com.binance.account.service.operationlog.IUserOperationLog;
import com.binance.account.vo.operationlog.UserOperationLogVo;
import com.binance.account.vo.security.request.*;
import com.binance.account.vo.security.response.UserOperationLogResultResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Log4j2
@RestController
public class UserOperationLogController implements UserOperationLogApi {

    @Resource
    private IUserOperationLog userOperationLog;

    @Override
    @SentinelResource(value = "/userOperationLog/query")
    public APIResponse<UserOperationLogResultResponse> listUserOperationLogs(@RequestBody @Validated APIRequest<UserOperationLogRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.queryUserOperationLogPage(request.getBody()));
    }

    @Override
    @SentinelResource(value = "/userOperationLog/query/user-view")
    public APIResponse<UserOperationLogResultResponse> listUserOperationLogsUserView(
            @RequestBody @Validated APIRequest<UserOperationLogUserViewRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.queryUserOperationLogPageUserView(request.getBody()));
    }

    @Override
    public APIResponse<UserOperationLogResultResponse> findTodaysUserOperationLogs(
            @RequestBody @Validated APIRequest<FindTodaysUserOperationLogsRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.findTodaysUserOperationLogs(request.getBody()));
    }

    @Override
    @SentinelResource(value = "/userOperationLog/countToday")
    public APIResponse<Long> countTodaysUserOperationLogs(
            @RequestBody @Validated APIRequest<CountTodaysUserOperationLogsRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.countTodaysUserOperationLogs(request.getBody()));

    }

    @Override
    @SentinelResource(value = "/userOperationLog/countLogin")
    public APIResponse<Long> countDistinctLogin(@RequestBody @Validated APIRequest<CountLoginRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.countDistinctLogin(request.getBody()));
    }

    @Override
	public APIResponse<UserOperationLogVo> queryDetail(@RequestBody @Validated APIRequest<UserIdAndIdRequest> request)
			throws Exception {
		return APIResponse.getOKJsonResult(userOperationLog.queryDetail(request.getBody()));
	}

    @Override
    public APIResponse<UserOperationLogVo> queryDetailWithUuid(@RequestBody @Validated APIRequest<QueryDetailWithUuidRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(userOperationLog.queryDetailWithUuid(request.getBody()));
    }
}
