package com.binance.account.service.user;

import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.request.EnableUserLVTByAdminRequest;
import com.binance.account.vo.user.response.SignLVTStatusResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;


public interface IUserLVT {

    APIResponse<Boolean> signLVTRiskAgreement(APIRequest<UserIdReq> request);

    APIResponse<Boolean> cancelSignLVT(APIRequest<UserIdReq> request);

    APIResponse<Boolean> enableUserLVTByAdmin(APIRequest<EnableUserLVTByAdminRequest> request) throws Exception;

    APIResponse<SignLVTStatusResponse> signLVTStatus(APIRequest<UserIdReq> request);

}
