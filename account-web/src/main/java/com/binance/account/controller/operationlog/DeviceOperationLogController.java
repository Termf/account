package com.binance.account.controller.operationlog;

import com.binance.account.api.DeviceOperationLogApi;
import com.binance.account.service.operationlog.IDeviceOperationLog;
import com.binance.account.vo.security.request.DeviceOperationLogRequest;
import com.binance.account.vo.security.response.DeviceOperationLogResultResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Log4j2
@RestController
public class DeviceOperationLogController implements DeviceOperationLogApi {

    @Resource
    private IDeviceOperationLog deviceOperationLog;

    @Override
    public APIResponse<DeviceOperationLogResultResponse> listDeviceOperationLogs(@RequestBody @Validated APIRequest<DeviceOperationLogRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(deviceOperationLog.queryDeviceOperationLogPage(request.getBody()));
    }

}
