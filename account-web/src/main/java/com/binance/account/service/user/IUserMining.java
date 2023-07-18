package com.binance.account.service.user;

import com.binance.account.vo.mining.request.CreateMingAccountRequest;
import com.binance.account.vo.mining.response.CreateMiningUserResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;


public interface IUserMining {

    APIResponse<CreateMiningUserResponse> createMiningAccount(APIRequest<CreateMingAccountRequest> request)
            throws Exception;



}
