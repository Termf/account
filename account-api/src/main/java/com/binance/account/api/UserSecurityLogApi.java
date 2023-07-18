package com.binance.account.api;

import com.binance.account.vo.security.request.*;
import com.binance.master.configs.FeignConfig;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.UserSecurityLogListResponse;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户安全日志")
@RequestMapping("/userSecurityLog")
public interface UserSecurityLogApi {

    @ApiOperation("查询用户安全日志")
    @PostMapping("/getUserSecurityLogList")
    APIResponse<GetUserSecurityLogResponse> getUserSecurityLogList(
            @RequestBody APIRequest<GetUserSecurityLogRequest> request) throws Exception;

    @ApiOperation("获取最后登录日志")
    @PostMapping("/getLastLoginLog")
    APIResponse<UserSecurityLogVo> getLastLoginLog(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("用户安全日志分页列表")
    @PostMapping("/getLogPage")
    APIResponse<GetUserSecurityLogResponse> getLogPage(@RequestBody APIRequest<UserSecurityRequest> request) throws Exception;
    
    @ApiOperation("根据ip获取用户安全日志列表")
    @PostMapping("/getLogByIp")
    APIResponse<UserSecurityLogListResponse> getLogByIp(@RequestBody APIRequest<IpPageRequest> request) throws Exception;
    
    @ApiOperation("获取ip地址关联的用户数")
    @PostMapping("/getLogByIpCount")
    APIResponse<List<Map<String, Object>>> getLogByIpCount(@RequestBody APIRequest<IpRequest> request) throws Exception;

    @ApiOperation("是否后台禁用")
    @PostMapping("/isBackendDisadbled")
    APIResponse<Boolean> isBackendDisadbled(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

}
