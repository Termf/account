package com.binance.account.service.user;

import com.binance.account.data.entity.user.UserCommonPermission;
import com.binance.account.vo.user.UserCommonPermissionVo;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.account.vo.user.request.SelectAllUserPermissionRequest;
import com.binance.account.vo.user.request.SelectUserPermissionByUserIdRequest;
import com.binance.account.vo.user.response.SelectAllUserPermissionResponse;

public interface IUserPermission {
    SelectAllUserPermissionResponse selectAllUserPermission(SelectAllUserPermissionRequest selectAllUserPermissionRequest);

    UserCommonPermissionVo selectUserPermissionByUserId(SelectUserPermissionByUserIdRequest selectUserPermissionByUserIdRequest);

    UserCommonPermission selectByUserId(Long userId);

    UserCommonPermission getUserPermissionByUserStatus(Long userStatus);

    UserTypeEnum getUserTypeByUserStatus(Long userStatus);

    Boolean selectByUserIdAndOperation(Long userId, UserPermissionOperationEnum userPermissionOperationEnum);

}
