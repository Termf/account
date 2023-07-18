package com.binance.account.service.user;

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
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2019/3/8
 */
public interface IUserMargin {

    APIResponse<MarginUserTypeResponse> allUserInfo(UserIdRequest request);

    APIResponse<MarginUserTypeResponse> globalUserInfo(UserIdRequest request);

    APIResponse<MainMarginAccountTransferResponse> subMainMarginAccountTransfer(MainMarginAccountTransferRequest body)throws Exception;


    CreateIsolatedMarginUserResp createIsolatedMarginUser(CreateIsolatedMarginUserReq req)throws Exception;


    GetIsolatedMarginUserListResp getIsolatedMarginUserList(GetIsolatedMarginUserListReq req)throws Exception;

    Long getRootUserIdByIsolatedMarginUserId(GetRootUserIdByIsolatedMarginUserIdReq req)throws Exception;


    Boolean checkIsolatedMarginRelationShip(CheckIsolatedMarginUserRelationShipReq req)throws Exception;




}
