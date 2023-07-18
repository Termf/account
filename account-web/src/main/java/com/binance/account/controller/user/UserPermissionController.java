package com.binance.account.controller.user;

import com.binance.account.api.UserPermissionApi;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserPermission;
import com.binance.account.vo.user.UserCommonPermissionVo;
import com.binance.account.vo.user.request.SelectAllUserPermissionRequest;
import com.binance.account.vo.user.request.SelectUserPermissionByUserIdRequest;
import com.binance.account.vo.user.response.SelectAllUserPermissionResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Log4j2
@RestController
public class UserPermissionController implements UserPermissionApi {

    @Resource
    private IUserPermission userPermission;


    @Override
    public APIResponse<SelectAllUserPermissionResponse> selectAllUserPermission(@RequestBody @Validated APIRequest<SelectAllUserPermissionRequest> request) throws Exception {
        SelectAllUserPermissionRequest selectAllUserPermissionRequest= request.getBody();
        return APIResponse.getOKJsonResult(userPermission.selectAllUserPermission(selectAllUserPermissionRequest));
    }

    @Override
    public APIResponse<UserCommonPermissionVo> selectUserPermissionByUserId(@RequestBody @Validated APIRequest<SelectUserPermissionByUserIdRequest> request) throws Exception {
        SelectUserPermissionByUserIdRequest selectUserPermissionByUserIdRequest= request.getBody();
        return APIResponse.getOKJsonResult(userPermission.selectUserPermissionByUserId(selectUserPermissionByUserIdRequest));
    }
}
