package com.binance.account.controller.user;

import com.binance.account.api.UserLVTApi;
import com.binance.account.service.user.IUserLVT;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.request.EnableUserLVTByAdminRequest;
import com.binance.account.vo.user.response.SignLVTStatusResponse;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserCardApi;
import com.binance.account.service.user.IUserCard;
import com.binance.account.vo.card.request.CreateCardAccountRequest;
import com.binance.account.vo.card.response.CreateCardAccountResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@RestController
@Monitored
@Log4j2
public class UserLVTController implements UserLVTApi {

    @Autowired
    private IUserLVT iUserLVT;

    @Override
    public APIResponse<Boolean> signLVTRiskAgreement(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUserLVT.signLVTRiskAgreement(request);
    }

    @Override
    public APIResponse<Boolean> enableUserLVTByAdmin(@Validated @RequestBody APIRequest<EnableUserLVTByAdminRequest> request) throws Exception {
        return iUserLVT.enableUserLVTByAdmin(request);
    }

    @Override
    public APIResponse<SignLVTStatusResponse> signLVTStatus(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUserLVT.signLVTStatus(request);
    }
}
