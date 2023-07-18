package com.binance.account.controller.user;

import javax.annotation.Resource;

import com.binance.account.vo.user.request.OauthResendEmailRequest;
import com.binance.account.vo.user.request.OpenIdActiveRequest;
import com.binance.account.vo.user.response.OpenIdActiveResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserOauthApi;
import com.binance.account.service.user.IOauth;
import com.binance.account.vo.user.request.BindOauthRequest;
import com.binance.account.vo.user.response.BindOauthResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@RestController
public class UserOauthController implements UserOauthApi {
    @Resource
    private IOauth iOauth;

    @Override
    public APIResponse<BindOauthResponse> oauthBind(@Validated @RequestBody APIRequest<BindOauthRequest> request)
            throws Exception {
        return iOauth.bind(request);
    }

    @Override
    public APIResponse<OpenIdActiveResponse> openIdActive(
            @Validated @RequestBody APIRequest<OpenIdActiveRequest> request) throws Exception {
        return iOauth.openIdActive(request);
    }

    @Override
    public APIResponse<String> resendVerifyEmail(@Validated @RequestBody APIRequest<OauthResendEmailRequest> request)
            throws Exception {
        return iOauth.resendVerifyEmail(request);
    }
}
