package com.binance.account.service.user;

import com.binance.account.vo.user.request.BindOauthRequest;
import com.binance.account.vo.user.request.OauthResendEmailRequest;
import com.binance.account.vo.user.request.OpenIdActiveRequest;
import com.binance.account.vo.user.response.BindOauthResponse;
import com.binance.account.vo.user.response.OpenIdActiveResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IOauth {
    APIResponse<BindOauthResponse> bind(APIRequest<BindOauthRequest> request) throws Exception;

    APIResponse<OpenIdActiveResponse> openIdActive(APIRequest<OpenIdActiveRequest> request);

    APIResponse<String> resendVerifyEmail(APIRequest<OauthResendEmailRequest> request) throws Exception;
}
