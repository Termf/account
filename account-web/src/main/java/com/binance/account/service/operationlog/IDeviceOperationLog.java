package com.binance.account.service.operationlog;

import com.binance.account.vo.security.request.DeviceOperationLogRequest;
import com.binance.account.vo.security.response.DeviceOperationLogResultResponse;

public interface IDeviceOperationLog {

    DeviceOperationLogResultResponse queryDeviceOperationLogPage(DeviceOperationLogRequest request) throws Exception;


}
