package com.binance.account.service.user;

import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;


public interface IUserWaas {

    APIResponse<Void> setToWaasAccount(APIRequest<UserIdRequest> request)
            throws Exception;



}
