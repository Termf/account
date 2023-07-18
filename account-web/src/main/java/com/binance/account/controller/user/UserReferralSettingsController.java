package com.binance.account.controller.user;

import com.binance.account.api.UserReferralSettingsApi;
import com.binance.account.service.certificate.IUserReferralSettings;
import com.binance.account.vo.user.request.UserReferralSettingsRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserReferralSettingsController implements UserReferralSettingsApi {
    @Resource
    private IUserReferralSettings userReferralSettings;

    @Override
    public APIResponse<?> submit(@RequestBody() @Validated APIRequest<UserReferralSettingsRequest> request) {
        return userReferralSettings.submit(request);
    }
}
