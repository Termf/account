package com.binance.account.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.reset.request.Reset2faNextStepRequest;
import com.binance.account.vo.reset.request.Reset2faStartValidatedRequest;
import com.binance.account.vo.reset.request.ResetResendEmailRequest;
import com.binance.account.vo.reset.request.ResetUploadInitRequest;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.account.vo.reset.response.Reset2faStartValidatedResponse;
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
@RequestMapping("/reset2fa")
public interface Reset2FaApi {


    /**
     * reset2fa 之前对前置校验当前是否能做reset2fa
     *
     * @param request
     * @return
     */
    @ApiOperation("初步检验是否能做reset2fa")
    @PostMapping("/pre/validated")
    APIResponse<Reset2faStartValidatedResponse> reset2faStartValidated(@Validated @RequestBody APIRequest<Reset2faStartValidatedRequest> request);

    /**
     * 重置流程的下一步
     *
     * @param request
     * @return
     */
    @ApiOperation("获取RESET-2FA流程下一步")
    @PostMapping("/flow/nextStep")
    APIResponse<Reset2faNextStepResponse> reset2faNextStepFlow(@Validated @RequestBody APIRequest<Reset2faNextStepRequest> request);

    /**
     * 邮件点击后初始化上传页面信息
     * @param request
     * @return
     */
    @ApiOperation("邮件点击后初始化上传页面信息")
    @PostMapping("/flow/openUploadEmail")
    APIResponse<Reset2faNextStepResponse> reset2faUploadEmailOpen(@Validated @RequestBody APIRequest<ResetUploadInitRequest> request);

    /**
     * 邮件点击后初始化上传页面信息
     * @param request
     * @return
     */
    @ApiOperation("邮件点击后初始化上传页面信息")
    @PostMapping("/flow/resendEmail")
    APIResponse<Reset2faNextStepResponse> sendEmailAgain(@Validated @RequestBody APIRequest<ResetResendEmailRequest> request);

}
