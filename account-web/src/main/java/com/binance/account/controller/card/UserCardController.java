package com.binance.account.controller.card;

import com.binance.account.api.UserCardApi;
import com.binance.account.service.user.IUserCard;
import com.binance.account.vo.card.request.CreateCardAccountRequest;
import com.binance.account.vo.card.response.CreateCardAccountResponse;
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
public class UserCardController implements UserCardApi {

    @Autowired
    private IUserCard userCardService;

    @Override
    public APIResponse<CreateCardAccountResponse> createCardAccount(@RequestBody @Validated APIRequest<CreateCardAccountRequest> request) throws Exception {
        return userCardService.createCardAccount(request);
    }
}
