package com.binance.account.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.api.UserEmailChangeApi;
import com.binance.account.service.user.IUserEmailChange;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.OldEmailCaptchaResponse;
import com.binance.account.vo.user.response.UserEmailChangeInitResponse;
import com.binance.account.vo.user.response.UserEmailChangeResponse;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class UserEmailChangeController implements UserEmailChangeApi {



    @Autowired
    private IUserEmailChange iUserEmailChange;



    @Override
    public APIResponse<UserEmailChangeInitResponse> initFlow(@Validated @RequestBody APIRequest<UserEmailChangeInitFlowRequest> request) {
        return iUserEmailChange.initFlow(request.getBody().getUserId(), request.getBody().getEmail(), request.getBody().getAvailableType());
    }


    @Override
    public APIResponse<Void> linkOldEmail(String flowId, Long userId) {
        return iUserEmailChange.linkOldEmail(flowId, userId);
    }

    @Override
    public APIResponse<UserEmailChangeInitResponse> linkNewEmail(String flowId, Long userId) {
        return iUserEmailChange.linkNewEmail(flowId, userId);
    }



    @Override
    public APIResponse<Void> linkOldEmail(@Validated @RequestBody APIRequest<UserEmailChangeLinkRequest> request) {
        return iUserEmailChange.linkOldEmailV2(request.getBody().getFlowId(), request.getBody().getUserId(), request.getBody().getSign());
    }

    @Override
    public APIResponse<OldEmailCaptchaResponse> validOldEmailCaptcha(@Validated @RequestBody APIRequest<OldEmailCaptchaRequest> request) {
        return iUserEmailChange.validOldEmailCaptcha(request);
    }


    @Override
    public APIResponse<String> confirmNewEmail(@Validated @RequestBody APIRequest<UserEmailChangeConfirmNewEmailRequest> request) {
        return iUserEmailChange.confirmNewEmailV2(request);
    }



    @Override
    public APIResponse<UserEmailChangeInitResponse> linkNewEmail(@Validated @RequestBody APIRequest<UserEmailChangeLinkRequest> request) {
        return iUserEmailChange.linkNewEmailV2(request.getBody().getFlowId(), request.getBody().getUserId(),request.getBody().getSign());
    }

    @Override
    public APIResponse<UserEmailChangeInitResponse> validNewEmailCaptcha(@Validated @RequestBody APIRequest<NewEmailCaptchaRequest> request) {
        return iUserEmailChange.validNewEmailCaptcha(request);
    }

    @Override
    public APIResponse<Void> sendOldEmail(String flowId, Long userId, String email) {
        return iUserEmailChange.resendEmail(userId, flowId, email, 1);
    }

    @Override
    public APIResponse<Void> sendNewEmail(String flowId, Long userId, String email) {
        return iUserEmailChange.resendEmail(userId, flowId, email, 2);
    }

    @Override
    public APIResponse<UserEmailChangeResponse> getEmailChangeList(@RequestBody APIRequest<UserEmailChangeRequest> request) {
        return iUserEmailChange.getEmailChangeList(request);
    }

    @Override
    public APIResponse<Void> updateUserEmailChangeByFlowId(@RequestBody APIRequest<UserEmailChangeRequest> request) {
        try {
            iUserEmailChange.updateUserEmailChangeByFlowId(request.getBody());
        } catch (Exception e) {
            log.error("UserEmailChangeController error is {},request is {}", e, JSONObject.toJSONString(request));
            return new APIResponse<>(APIResponse.Status.OK, GeneralCode.ERROR.getCode());
        }
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<String> confirmNewEmailV3(@Validated @RequestBody APIRequest<NewEmailConfirmRequest> request) {
        return iUserEmailChange.confirmNewEmailV3(request);
    }

}
