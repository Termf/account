package com.binance.account.controller.user;

import com.binance.account.api.UserMarginApi;
import com.binance.account.service.user.IUserMargin;
import com.binance.account.vo.margin.request.CheckIsolatedMarginUserRelationShipReq;
import com.binance.account.vo.margin.request.CreateIsolatedMarginUserReq;
import com.binance.account.vo.margin.request.GetIsolatedMarginUserListReq;
import com.binance.account.vo.margin.request.GetRootUserIdByIsolatedMarginUserIdReq;
import com.binance.account.vo.margin.response.CreateIsolatedMarginUserResp;
import com.binance.account.vo.margin.response.GetIsolatedMarginUserListResp;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.request.MainMarginAccountTransferRequest;
import com.binance.account.vo.user.response.MainMarginAccountTransferResponse;
import com.binance.account.vo.user.response.MarginUserTypeResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lufei
 * @date 2019/3/8
 */
@RestController
@Monitored
public class UserMarginController implements UserMarginApi {

    @Autowired
    private IUserMargin userMargin;

    public APIResponse<MarginUserTypeResponse> allUserInfo(@RequestBody @Validated APIRequest<UserIdRequest> request)
            throws Exception {
        return userMargin.allUserInfo(request.getBody());
    }

    @Override
    public APIResponse<MarginUserTypeResponse> globalUserInfo(@RequestBody @Validated APIRequest<UserIdRequest> request) throws Exception {
        return userMargin.globalUserInfo(request.getBody());
    }

    public APIResponse<MainMarginAccountTransferResponse> subMainMarginAccountTransfer(@RequestBody @Validated APIRequest<MainMarginAccountTransferRequest> request) throws Exception{
        return userMargin.subMainMarginAccountTransfer(request.getBody());
    }

    @Override
    public APIResponse<CreateIsolatedMarginUserResp> createIsolatedMarginUser(@RequestBody @Validated APIRequest<CreateIsolatedMarginUserReq> request) throws Exception {
        CreateIsolatedMarginUserReq createIsolatedMarginUserReq=request.getBody();
        CreateIsolatedMarginUserResp createIsolatedMarginUserResp= userMargin.createIsolatedMarginUser(createIsolatedMarginUserReq);
        return APIResponse.getOKJsonResult(createIsolatedMarginUserResp);
    }

    @Override
    public APIResponse<GetIsolatedMarginUserListResp> getIsolatedMarginUserList(@RequestBody @Validated APIRequest<GetIsolatedMarginUserListReq> request) throws Exception {
        GetIsolatedMarginUserListReq getIsolatedMarginUserListReq=request.getBody();
        GetIsolatedMarginUserListResp GetIsolatedMarginUserListResp= userMargin.getIsolatedMarginUserList(getIsolatedMarginUserListReq);
        return APIResponse.getOKJsonResult(GetIsolatedMarginUserListResp);
    }

    @Override
    public APIResponse<Long> getRootUserIdByIsolatedMarginUserId(@RequestBody @Validated APIRequest<GetRootUserIdByIsolatedMarginUserIdReq> request) throws Exception {
        GetRootUserIdByIsolatedMarginUserIdReq getRootUserIdByIsolatedMarginUserIdReq=request.getBody();
        Long resp= userMargin.getRootUserIdByIsolatedMarginUserId(getRootUserIdByIsolatedMarginUserIdReq);
        return APIResponse.getOKJsonResult(resp);

    }

    @Override
    public APIResponse<Boolean> checkIsolatedMarginRelationShip(@RequestBody @Validated  APIRequest<CheckIsolatedMarginUserRelationShipReq> request) throws Exception {
        CheckIsolatedMarginUserRelationShipReq checkIsolatedMarginUserRelationShipReq=request.getBody();
        Boolean resp= userMargin.checkIsolatedMarginRelationShip(checkIsolatedMarginUserRelationShipReq);
        return APIResponse.getOKJsonResult(resp);
    }


}
