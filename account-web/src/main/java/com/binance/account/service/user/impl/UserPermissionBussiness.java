package com.binance.account.service.user.impl;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserCommonPermission;
import com.binance.account.data.mapper.user.UserCommonPermissionMapper;
import com.binance.account.service.user.IUserPermission;
import com.binance.account.vo.user.UserCommonPermissionVo;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.SelectAllUserPermissionRequest;
import com.binance.account.vo.user.request.SelectUserPermissionByUserIdRequest;
import com.binance.account.vo.user.response.SelectAllUserPermissionResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class UserPermissionBussiness implements IUserPermission {

    @Autowired
    private UserCommonPermissionMapper userCommonPermissionMapper;

    @Autowired
    private UserCommonBusiness userCommonBusiness;



    public UserCommonPermission selectByUserType(UserTypeEnum userType){
        return userCommonPermissionMapper.selectByPrimaryKey(userType.name());
    }

    public List<UserCommonPermission> selectAll(){
        return userCommonPermissionMapper.selectAll();
    }

    @Override
    public UserCommonPermission selectByUserId(Long userId){
        UserTypeEnum userTypeEnum=getUserType(userId);
        return userCommonPermissionMapper.selectByPrimaryKey(userTypeEnum.name());
    }

    @Override
    public UserCommonPermission getUserPermissionByUserStatus(Long userStatus) {
        UserTypeEnum userTypeEnum=getUserTypeByUserStatus(userStatus);
        return userCommonPermissionMapper.selectByPrimaryKey(userTypeEnum.name());
    }

    @Override
    public Boolean selectByUserIdAndOperation(Long userId, UserPermissionOperationEnum userPermissionOperationEnum) {
        UserCommonPermission userCommonPermission=selectByUserId(userId);
        switch (userPermissionOperationEnum) {
            case ENABLE_DEPOSIT:
                return userCommonPermission.getEnableDeposit();
            case ENABLE_WITHDRAW:
                return userCommonPermission.getEnableWithdraw();
            case ENABLE_TRADE:
                return userCommonPermission.getEnableTrade();
            case ENABLE_TRANSFER:
                return userCommonPermission.getEnableTransfer();
            case ENABLE_SUB_TRANSFER:
                return userCommonPermission.getEnableSubTransfer();
            case ENABLE_CREATE_APIKEY:
                return userCommonPermission.getEnableCreateApikey();
            case ENABLE_LOGIN:
                return userCommonPermission.getEnableLogin();
            case ENABLE_CREATE_MARGIN:
                return userCommonPermission.getEnableCreateMargin();
            case ENABLE_CREATE_FUTURES:
                return userCommonPermission.getEnableCreateFutures();
            case ENABLE_CREATE_FIAT:
                return userCommonPermission.getEnableCreateFiat();
            case ENABLE_CREATE_ISOLATED_MARGIN:
                return userCommonPermission.getEnableCreateIsolatedMargin();
            case ENABLE_CREATE_SUB_ACCOUNT:
                return userCommonPermission.getEnableCreateSubAccount();
            case ENABLE_PARENT_ACCOUNT:
                return userCommonPermission.getEnableParentAccount();
            case ENABLE_BROKER_PARENT_ACCOUNT:
                return userCommonPermission.getEnableBrokerParentAccount();
            case ENABLE_CREATE_BROKER_SUB_ACCOUNT:
                return userCommonPermission.getEnableCreateBrokerSubAccount();
            default:
                throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
    }

    public UserTypeEnum getUserType(Long userId){
        User user = userCommonBusiness.checkAndGetUserById(userId);
        UserTypeEnum userTypeEnum=getUserTypeByUserStatus(user.getStatus());
        return userTypeEnum;
    }
    
    @Override
    public UserTypeEnum getUserTypeByUserStatus(Long userStatus){
        //先不要判断broker账号，因为broker权限和字母账号一样，再做区分没有意义，而且
        //会引起调用方错误
        UserTypeEnum userTypeEnum=UserTypeEnum.NORMAL;
        UserStatusEx userStatusEx=new UserStatusEx(userStatus);
        if(userStatusEx.getIsAssetSubUser()){
            userTypeEnum=UserTypeEnum.ASSET_SUB;
        }else if(userStatusEx.getIsMarginUser()){
            userTypeEnum=UserTypeEnum.MARGIN;
        }else if(userStatusEx.getIsBrokerSubUserFunctionEnabled()){
            userTypeEnum=UserTypeEnum.BROKER_PARENT;
        }else if(userStatusEx.getIsBrokerSubUser()){
            userTypeEnum=UserTypeEnum.BROKER_SUB;
        }else if(userStatusEx.getIsSubUserFunctionEnabled()){
            userTypeEnum=UserTypeEnum.PARENT;
        }else if(userStatusEx.getIsFutureUser()){
            userTypeEnum=UserTypeEnum.FUTURE;
        }else if(userStatusEx.getIsFiatUser()){
            userTypeEnum=UserTypeEnum.FIAT;
        }else if(userStatusEx.getIsNoEmailSubUser()){
            // 无邮箱子账号也是子账号，NO_EMAIL_SUB需要放在SUB前面
            userTypeEnum=UserTypeEnum.NO_EMAIL_SUB;
        }else if(userStatusEx.getIsSubUser()){
            userTypeEnum=UserTypeEnum.SUB;
        }else if(userStatusEx.getIsMiningUser()){
            userTypeEnum=UserTypeEnum.MINING;
        }else if(userStatusEx.getIsIsolatedMarginUser()){
            userTypeEnum=UserTypeEnum.ISOLATED_MARGIN;
        }else if(userStatusEx.getIsCardUser()){
            userTypeEnum=UserTypeEnum.CARD;
        }else if(userStatusEx.getIsWaasUser()){
            userTypeEnum=UserTypeEnum.WAAS;
        }
        return userTypeEnum;
    }

    @Override
    public SelectAllUserPermissionResponse selectAllUserPermission(SelectAllUserPermissionRequest selectAllUserPermissionRequest) {
        List<UserCommonPermission> userCommonPermissionList=selectAll();
        SelectAllUserPermissionResponse resp=new SelectAllUserPermissionResponse();
        if(CollectionUtils.isEmpty(userCommonPermissionList)){
            return resp;
        }
        List<UserCommonPermissionVo> userCommonPermissionVoList= Lists.transform(userCommonPermissionList, new Function<UserCommonPermission, UserCommonPermissionVo>() {
            @Override
            public UserCommonPermissionVo apply(@Nullable UserCommonPermission userCommonPermission) {
                UserCommonPermissionVo vo=new UserCommonPermissionVo();
                BeanUtils.copyProperties(userCommonPermission,vo);
                vo.setUserType(UserTypeEnum.valueOf(userCommonPermission.getUserType()));
                return vo;
            }
        });
        resp.setUserCommonPermissionVoList(userCommonPermissionVoList);
        return resp;
    }

    @Override
    public UserCommonPermissionVo selectUserPermissionByUserId(SelectUserPermissionByUserIdRequest selectUserPermissionByUserIdRequest) {
        UserCommonPermission userCommonPermission=selectByUserId(selectUserPermissionByUserIdRequest.getUserId());
        UserCommonPermissionVo vo=new UserCommonPermissionVo();
        if(null==userCommonPermission){
            return null;
        }
        BeanUtils.copyProperties(userCommonPermission,vo);
        vo.setUserType(UserTypeEnum.valueOf(userCommonPermission.getUserType()));
        return vo;
    }
}
