package com.binance.account.service.operationlog;

import com.binance.account.vo.operationlog.UserOperationLogVo;
import com.binance.account.vo.security.request.CountLoginRequest;
import com.binance.account.vo.security.request.CountTodaysUserOperationLogsRequest;
import com.binance.account.vo.security.request.FindTodaysUserOperationLogsRequest;
import com.binance.account.vo.security.request.QueryDetailWithUuidRequest;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.security.request.UserOperationLogRequest;
import com.binance.account.vo.security.request.UserOperationLogUserViewRequest;
import com.binance.account.vo.security.response.UserOperationLogResultResponse;


public interface IUserOperationLog {

    UserOperationLogResultResponse queryUserOperationLogPage(UserOperationLogRequest request) throws Exception;

    UserOperationLogResultResponse queryUserLoginLogs(UserOperationLogUserViewRequest request) throws Exception;

    UserOperationLogResultResponse queryUserOperationLogPageUserView(UserOperationLogUserViewRequest request) throws Exception;

    UserOperationLogResultResponse findTodaysUserOperationLogs(FindTodaysUserOperationLogsRequest request);

    Long countTodaysUserOperationLogs(CountTodaysUserOperationLogsRequest request);
    
    UserOperationLogVo queryDetail(UserIdAndIdRequest request);

    Long countDistinctLogin(CountLoginRequest request);

    UserOperationLogVo queryDetailWithUuid(QueryDetailWithUuidRequest request);
}
