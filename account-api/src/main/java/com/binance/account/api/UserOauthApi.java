package com.binance.account.api;

import javax.validation.Valid;

import com.binance.account.vo.user.request.OauthResendEmailRequest;
import com.binance.account.vo.user.request.OpenIdActiveRequest;
import com.binance.account.vo.user.response.OpenIdActiveResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.user.request.BindOauthRequest;
import com.binance.account.vo.user.response.BindOauthResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/oauth")
@Api(value = "oauth")
public interface UserOauthApi {

    @ApiOperation("绑定第三方")
    @PostMapping("/bind")
    APIResponse<BindOauthResponse> oauthBind(@Valid @RequestBody APIRequest<BindOauthRequest> request)
            throws Exception;

    @ApiOperation("激活oauth openId")
    @PostMapping("/active")
    APIResponse<OpenIdActiveResponse> openIdActive(@Validated @RequestBody APIRequest<OpenIdActiveRequest> request)
            throws Exception;

    @ApiOperation("重发oauth激活邮件")
    @PostMapping("/resend")
    APIResponse<String> resendVerifyEmail(@Validated @RequestBody APIRequest<OauthResendEmailRequest> request)
            throws Exception;
}
