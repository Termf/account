package com.binance.account.api;

import com.binance.account.vo.security.request.EnableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.EnableFastWithdrawSwitchResponse;
import com.binance.account.vo.user.UserCommonPermissionVo;
import com.binance.account.vo.user.request.MainMarginAccountTransferRequest;
import com.binance.account.vo.user.request.SelectAllUserPermissionRequest;
import com.binance.account.vo.user.request.SelectUserPermissionByUserIdRequest;
import com.binance.account.vo.user.response.MainMarginAccountTransferResponse;
import com.binance.account.vo.user.response.MarginUserTypeResponse;
import com.binance.account.vo.user.response.SelectAllUserPermissionResponse;
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


@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/user/permission")
@Api(value = "账户权限")
public interface UserPermissionApi {

    @ApiOperation("获取所有权限配置")
    @PostMapping("/selectAllUserPermission")
    APIResponse<SelectAllUserPermissionResponse> selectAllUserPermission(@RequestBody APIRequest<SelectAllUserPermissionRequest> request) throws Exception;


    @ApiOperation("获取权限配置通过userid")
    @PostMapping("/selectUserPermissionByUserId")
    APIResponse<UserCommonPermissionVo> selectUserPermissionByUserId(@RequestBody APIRequest<SelectUserPermissionByUserIdRequest> request) throws Exception;




}
