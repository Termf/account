package com.binance.account.controller.subuser;

import com.binance.account.aop.MarginValidate;
import com.binance.account.api.SubUserAdminApi;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.vo.subuser.request.AssetSubUserToCommonRequest;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.SubUserIdReq;
import com.binance.account.vo.subuser.response.ParentUserSubUsersResp;
import com.binance.account.vo.subuser.response.SubUserParentUserResp;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Fei.Huang on 2018/10/12.
 */
@RestController
public class SubUserAdminController implements SubUserAdminApi {

    @Autowired
    private ISubUserAdmin iSubUserAdmin;
    @MarginValidate(userId = "#request.body.parentUserId")
    @Override
    public APIResponse<Boolean> enableSubUserFunction(@Validated() @RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUserAdmin.enableSubUserFunction(request);
    }

    @Override
    public APIResponse<Boolean> disableSubUserFunction(@Validated() @RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUserAdmin.disableSubUserFunction(request);
    }

    @Override
    public APIResponse<Boolean> bindParentSubUser(@Validated() @RequestBody() APIRequest<BindingParentSubUserReq> request) throws Exception {
        return iSubUserAdmin.bindParentSubUser(request);
    }

    @Override
    public APIResponse<SubUserParentUserResp> queryBySubUserId(@Validated() @RequestBody() APIRequest<SubUserIdReq> request) throws Exception {
        return iSubUserAdmin.queryBySubUserId(request);
    }

    @Override
    public APIResponse<ParentUserSubUsersResp> queryByParentUserId(@Validated() @RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUserAdmin.queryByParentUserId(request);
    }

    @Override
    public APIResponse<Boolean> updateSubUserRemark(@Validated() @RequestBody() APIRequest<SubUserIdReq> request) throws Exception {
        return iSubUserAdmin.updateSubUserRemark(request);
    }

    @Override
    public APIResponse<Long> countSubUsersByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception {
        return iSubUserAdmin.countSubUsersByParentUserId(request);
    }

    @Override
    public APIResponse<Void> assetSubUserToCommon(APIRequest<AssetSubUserToCommonRequest> request) throws Exception {
        return iSubUserAdmin.assetSubUserToCommon(request);
    }
}