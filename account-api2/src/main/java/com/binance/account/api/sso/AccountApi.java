package com.binance.account.api.sso;

import com.binance.account.vo.datamigration.response.DataMigrationUserIdResponse;
import com.binance.account.vo.sso.*;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Fei.Huang on 2018/4/11.
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("SSO同步数据临时接口")
@RequestMapping("/sso")
public interface AccountApi {

    @ApiOperation("激活账号")
    @PostMapping("/activationEmail")
    APIResponse<DataMigrationUserIdResponse> activationEmail(@RequestParam("email") String email) throws Exception;

    @ApiOperation("禁用账号")
    @PostMapping("/forbiddenEmail")
    APIResponse<DataMigrationUserIdResponse> forbiddenEmail(@RequestParam("email") String email) throws Exception;

    @ApiOperation("禁用交易")
    @PostMapping("/forbiddenTradeByEmail")
    APIResponse<DataMigrationUserIdResponse> forbiddenTradeByEmail(@RequestParam("email") String email)
            throws Exception;

    @ApiOperation("更新用户密码")
    @PostMapping("/updateUserPassword")
    APIResponse<DataMigrationUserIdResponse> updateUserPassword(@RequestParam UserVo userVo) throws Exception;

    @ApiOperation("重置支付密码、登录密码错误次数")
    @PostMapping("/resetPayPwdFailNumAndPasswordFailNum")
    APIResponse<DataMigrationUserIdResponse> resetPayPwdFailNumAndPasswordFailNum(@RequestParam("userId") Long userId)
            throws Exception;

    @ApiOperation("保存密码更新日志")
    @PostMapping("/insertPasswordLog")
    APIResponse<DataMigrationUserIdResponse> insertPasswordLog(@RequestBody PasswordLogVo passwordLogVo) throws Exception;

    @ApiOperation("登录密码错误次数+1")
    @PostMapping("/loginFailedCountIncrement")
    APIResponse<DataMigrationUserIdResponse> loginFailedCountIncrement(@RequestParam("email") String email)
            throws Exception;

    @ApiOperation("重置登录密码错误次数")
    @PostMapping("/loginFailedCountReset")
    APIResponse<DataMigrationUserIdResponse> loginFailedCountReset(@RequestParam("email") String email)
            throws Exception;

    @ApiOperation("保存用户日志（登录日志）")
    @PostMapping("/insertUserLog")
    APIResponse<DataMigrationUserIdResponse> insertUserLog(@RequestBody UserLogVo userLogVo) throws Exception;

    @ApiOperation("添加用户IP地址")
    @PostMapping("/insertUserIp")
    APIResponse<DataMigrationUserIdResponse> insertUserIp(@RequestParam("email") String email,
                                                          @RequestParam("ipAddress") String ipAddress) throws Exception;

    @ApiOperation("删除用户IP地址")
    @PostMapping("/deleteUserIp")
    APIResponse<DataMigrationUserIdResponse> deleteUserIp(@RequestParam("email") String email,
                                                          @RequestParam("ipAddress") String ipAddress) throws Exception;

    @ApiOperation("创建一个账号")
    @PostMapping("/insertUser")
    APIResponse<DataMigrationUserIdResponse> insertUser(@RequestBody UserVo userVo) throws Exception;

    @ApiOperation("创建一个账号：安全相关")
    @PostMapping("/insertUserSecurity")
    APIResponse<DataMigrationUserIdResponse> insertUserSecurity(@RequestBody UserSecurityVo userSecurityVo)
            throws Exception;

    @ApiOperation("创建一个账号：数据相关")
    @PostMapping("/insertUserData")
    APIResponse<DataMigrationUserIdResponse> insertUserData(@RequestBody UserDataVo userDataVo) throws Exception;
}
