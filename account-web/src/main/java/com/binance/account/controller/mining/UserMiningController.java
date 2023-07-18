package com.binance.account.controller.mining;

import com.binance.account.api.UserMiningApi;
import com.binance.account.service.user.IUserMining;
import com.binance.account.vo.mining.request.CreateMingAccountRequest;
import com.binance.account.vo.mining.response.CreateMiningUserResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Monitored
@Log4j2
public class UserMiningController implements UserMiningApi {

    @Autowired
    private IUserMining userMining;

    @Override
    public APIResponse<CreateMiningUserResponse> createMiningAccount(@RequestBody @Validated APIRequest<CreateMingAccountRequest> request) throws Exception {
        return userMining.createMiningAccount(request);
    }
}
