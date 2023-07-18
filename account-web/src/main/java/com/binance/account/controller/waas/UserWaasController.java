package com.binance.account.controller.waas;

import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserWaasApi;
import com.binance.account.service.user.IUserWaas;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@RestController
@Monitored
@Log4j2
public class UserWaasController implements UserWaasApi {

    @Autowired
    private IUserWaas userWaas;

    @Override
    public APIResponse<Void> setToWaasAccount(@RequestBody @Validated APIRequest<UserIdRequest> request) throws Exception {
        return userWaas.setToWaasAccount(request);
    }
}
