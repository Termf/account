package com.binance.account.api;


import com.binance.account.vo.security.request.DeviceOperationLogRequest;
import com.binance.account.vo.security.response.DeviceOperationLogResultResponse;
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
@RequestMapping(value = "/deviceOperationLog")
@Api(value = "设备行为日志")
public interface DeviceOperationLogApi {

    @ApiOperation("查询设备行为日志")
    @PostMapping("/query")
    APIResponse<DeviceOperationLogResultResponse> listDeviceOperationLogs(
            @RequestBody APIRequest<DeviceOperationLogRequest> request) throws Exception;

}
