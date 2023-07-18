package com.binance.account.service.user.impl;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.IUserPermission;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.utils.BitUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.service.user.IUserWaas;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Monitored
public class UserWaasBusiness implements IUserWaas {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IUserPermission userPermission;
    @Autowired
    private UserCommonBusiness userCommonBusiness;

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Void> setToWaasAccount(APIRequest<UserIdRequest> request) throws Exception {
        final UserIdRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        log.info("set to waas account with userId={}", userId);

        final User user = userCommonBusiness.checkAndGetUserById(userId);

        // 判断user的userType，只有normal时可以设置为waas账户。否则取userType时会错误
        UserTypeEnum userTypeEnum = userPermission.getUserTypeByUserStatus(user.getStatus());
        if (!userTypeEnum.equals(UserTypeEnum.NORMAL)) {
            log.error("set to waas account with userId={} failed, current accountType is {}", userId, userTypeEnum.name());
            throw new BusinessException(String.format("can not set to waas account, current type is %s", userTypeEnum.name()));    
        }
        
        User updateDO = new User();
        updateDO.setStatus(BitUtils.enable(user.getStatus(), AccountCommonConstant.USER_IS_WAAS_USER));
        updateDO.setEmail(user.getEmail());
        userMapper.updateByEmail(updateDO);

        log.info("set to waas account with userId={} succeed", userId);
        return APIResponse.getOKJsonResult();
    }


}
