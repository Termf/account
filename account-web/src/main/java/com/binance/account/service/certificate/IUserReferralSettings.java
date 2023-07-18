package com.binance.account.service.certificate;

import com.binance.account.vo.user.request.UserReferralSettingsRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface IUserReferralSettings {
    APIResponse<?> submit(@RequestBody() APIRequest<UserReferralSettingsRequest> request);
}
