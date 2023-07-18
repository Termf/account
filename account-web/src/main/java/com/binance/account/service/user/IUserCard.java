package com.binance.account.service.user;

import com.binance.account.vo.card.request.CreateCardAccountRequest;
import com.binance.account.vo.card.response.CreateCardAccountResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;


public interface IUserCard {

    APIResponse<CreateCardAccountResponse> createCardAccount(APIRequest<CreateCardAccountRequest> request)
            throws Exception;



}
