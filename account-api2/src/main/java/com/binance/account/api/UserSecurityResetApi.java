package com.binance.account.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.reset.ResetAnswerLogVo;
import com.binance.account.vo.reset.UserSecurityResetVo;
import com.binance.account.vo.reset.request.ResetAnswerArg;
import com.binance.account.vo.reset.request.ResetApplyTimesArg;
import com.binance.account.vo.reset.request.ResetAuditArg;
import com.binance.account.vo.reset.request.ResetIdArg;
import com.binance.account.vo.reset.request.ResetLastArg;
import com.binance.account.vo.reset.request.ResetPendingArg;
import com.binance.account.vo.reset.request.ResetUserIdArg;
import com.binance.account.vo.reset.response.ResetAnswerRet;
import com.binance.account.vo.reset.response.ResetApplyTimesRet;
import com.binance.account.vo.reset.response.ResetIdRet;
import com.binance.account.vo.reset.response.ResetLastRet;
import com.binance.account.vo.security.request.SecurityResetFaceTokenRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author liliang1
 * @date 2018-08-27 13:47
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户重置2FA")
@RequestMapping("/userSecurityReset")
public interface UserSecurityResetApi {

    /**
     * 查询用户的重置流程是否处于审核中
     * @param request
     * @return
     */
    @ApiOperation("获取用户当前重置流程是否正在申请审核中")
    @PostMapping("/isPending")
    APIResponse<Boolean> securityResetIsPending(@Validated @RequestBody APIRequest<ResetPendingArg> request);


    @Deprecated
    @ApiOperation("发送重置流程成功/失败的通知邮件")
    @PostMapping("/send/endStatusNotifyEmail")
    APIResponse sendResetEndStatusNotifyEmail(@RequestBody APIRequest<SecurityResetFaceTokenRequest> request);

    /**
     * 获取最近一次重置记录的一些信息
     * @param request
     * @return
     */
    @ApiOperation("获取用户最近一次重置申请信息")
    @PostMapping("/lastReset")
    APIResponse<ResetLastRet> getLastSecurityReset(@Validated @RequestBody APIRequest<ResetLastArg> request);

    /**
     * 根据重置流程ID获取重置流程的一些信息
     * @param request
     * @return
     */
    @ApiOperation("根据重置记录的ID获取信息")
    @PostMapping("/getRestById")
    APIResponse<ResetIdRet> getResetById(@Validated @RequestBody APIRequest<ResetIdArg> request);


    /**
     * 初始化2FA重置流程，并且发送通知邮件
     * @param request
     * @return
     */
    @ApiOperation("初始化发送2FA的重置流程邮件")
    @PostMapping("/send/initResetEmail")
    APIResponse<?> sendInitResetEmail(@Validated @RequestBody APIRequest<ResetLastArg> request);

    /**
     * 重置流程回答问题
     * @param request
     * @return
     */
    @ApiOperation("重置流程回答问题")
    @PostMapping("/answerQuestion")
    APIResponse<ResetAnswerRet> answerQuestion(@Validated @RequestBody APIRequest<ResetAnswerArg> request);

    /**
     * 取消重置申请
     * @param request
     * @return
     */
    @ApiOperation("取消重置流程申请")
    @PostMapping("/cancelReset")
    APIResponse<?> cancelSecurityReset(@Validated @RequestBody APIRequest<ResetLastArg> request);

    /**
     * 过度接口，以后建议直接使用 {@link UserFaceApi#resendFaceEmail(APIRequest)}
     * @param request
     * @return
     */
    @Deprecated
    @ApiOperation("重置流程重发人脸识别邮件")
    @PostMapping("/send/resetFaceEmail")
    APIResponse<?> sendResetFaceEmail(@Validated @RequestBody APIRequest<ResetLastArg> request);

    /**
     * 查询用户的重置流程的答题结果
     * @param request
     * @return
     */
    @ApiOperation("重置流程中的答题信息")
    @PostMapping("/answerLogs")
    APIResponse<List<ResetAnswerLogVo>> getResetAnswerLogs(@Validated @RequestBody APIRequest<ResetIdArg> request);


    /**
     * 获取用户申请做重置流程的次数信息
     * @param request
     * @return
     */
    @ApiOperation("查询某一用户做的重置流程次数信息")
    @PostMapping("/applyTimes")
    APIResponse<ResetApplyTimesRet> getResetApplyTimes(@Validated @RequestBody APIRequest<ResetApplyTimesArg> request);


    /**
     * 根据ID获取重置记录
     * @param request
     * @return
     */
    @ApiOperation("根据ID获取重置流程信息")
    @PostMapping("/getVoById")
    APIResponse<UserSecurityResetVo> getVoById(@Validated @RequestBody APIRequest<ResetIdArg> request);

    /**
     * 重置流程审核
     * @param request
     * @return
     */
    @ApiOperation("重置流程审核")
    @PostMapping("/resetAudit")
    APIResponse<?> resetAudit(@Validated @RequestBody APIRequest<ResetAuditArg> request);

    /**
     * 查询重置流程列表信息
     * @param request
     * @return
     */
    @ApiOperation("获取重置流程列表")
    @PostMapping("/getResetList")
    APIResponse<SearchResult<UserSecurityResetVo>> getResetList(@Validated @RequestBody APIRequest<ResetModularQuery> request);

    /**
     * 查询用户下的所有重置流程
     * @param request
     * @return
     */
    @ApiOperation("获取用户的所有重置流程信息")
    @PostMapping("/getUserAllReset")
    APIResponse<List<UserSecurityResetVo>> getUserAllReset(@Validated @RequestBody APIRequest<ResetUserIdArg> request);


}
