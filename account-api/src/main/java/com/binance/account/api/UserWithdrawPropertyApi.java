package com.binance.account.api;

import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockLogRequest;
import com.binance.account.vo.withdraw.request.WithdrawFaceInHoursRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawFaceTipResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockLogResponse;
import com.binance.master.configs.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.withdraw.request.UserWithdrawLockAmountRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawLockAmountResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockResponse;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/withdrawProperty")
@Api(value = "用户提现属性")
public interface UserWithdrawPropertyApi {

    @ApiOperation(notes = "锁定提现额度", nickname = "lock withdraw", value = "锁定提现额度，Return:total amount")
    @PostMapping("/lock")
    APIResponse<UserWithdrawLockResponse> lock(@RequestBody APIRequest<UserWithdrawLockRequest> request);

    @ApiOperation(notes = "解锁提现额度", nickname = "unlock withdraw", value = "解锁提现额度")
    @PostMapping("/unlock")
    APIResponse<UserWithdrawLockResponse> unlock(@RequestBody APIRequest<UserWithdrawLockRequest> request);
    
    @ApiOperation(notes = "获取锁定数量", nickname = "get lock amount", value = "获取锁定数量")
    @PostMapping("/getLockAmount")
    APIResponse<UserWithdrawLockAmountResponse> getLockAmount(@RequestBody APIRequest<UserWithdrawLockAmountRequest> request);

    /**
     * 用于检查用户当前是否需要做提现人脸识别，如果需要，根据当前进度返回对应状态和提示语
     * @param request
     * @return
     */
    @ApiOperation("提现风控是否需要人脸识别检查")
    @PostMapping("/check/withdrawFace")
    APIResponse<UserWithdrawFaceTipResponse> checkWithdrawFaceStatus(@Validated @RequestBody APIRequest<UserIdRequest> request);

    @ApiOperation("当前时间与上次验证通过的提币人脸识别加设定时间之内")
    @PostMapping("/check/withdrawFace/compareTime")
    APIResponse<Boolean> checkWithdrawFaceInHours(@Validated @RequestBody APIRequest<WithdrawFaceInHoursRequest> request);

    @ApiOperation(notes = "查询锁定/解锁日志", nickname = "queryLockLog", value = "查询锁定/解锁日志")
    @PostMapping("/queryLockLog")
    APIResponse<UserWithdrawLockLogResponse> queryLockLog(@Validated @RequestBody APIRequest<UserWithdrawLockLogRequest> request) throws Exception;

}
