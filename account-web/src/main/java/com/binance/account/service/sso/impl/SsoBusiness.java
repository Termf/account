package com.binance.account.service.sso.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.sso.ISso;
import com.binance.account.vo.datamigration.request.DataMigrationUserIdRequest;
import com.binance.account.vo.datamigration.response.DataMigrationUserIdResponse;
import com.binance.account.vo.sso.PasswordLogVo;
import com.binance.account.vo.sso.UserDataVo;
import com.binance.account.vo.sso.UserLogVo;
import com.binance.account.vo.sso.UserSecurityVo;
import com.binance.account.vo.sso.UserVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

/**
 * Created by Fei.Huang on 2018/4/11.
 * <p>
 * 尝试根据UserId同步单个用户全量数据，若有问题再改为逻辑细粒度较低的数据同步
 */
@Log4j2
@Service
public class SsoBusiness implements ISso {

    @Autowired
    private UserMapper userMapper;

    @Override
    public APIResponse<DataMigrationUserIdResponse> activationEmail(String email) throws Exception {
        log.info("activationEmail email:{}", email);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> forbiddenEmail(String email) throws Exception {
        log.info("forbiddenEmail email:{}", email);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> forbiddenTradeByEmail(String email) throws Exception {
        log.info("forbiddenTradeByEmail email:{}", email);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> updateUserPassword(UserVo user) throws Exception {
        notNullCheck(user);
        log.info("updateUserPassword userId:{}", user.getUserId());
        return commonSynchronByUserId(user.getUserId());
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> resetPayPwdFailNumAndPasswordFailNum(Long userId) throws Exception {
        log.info("resetPayPwdFailNumAndPasswordFailNum userId:{}", userId);
        return commonSynchronByUserId(userId);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertPasswordLog(PasswordLogVo passwordLog) throws Exception {
        notNullCheck(passwordLog);
        log.info("insertPasswordLog userId:{}", passwordLog.getUserId());
        return commonSynchronByUserId(passwordLog.getUserId());
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> loginFailedCountIncrement(String email) throws Exception {
        log.info("loginFailedCountIncrement email:{}", email);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> loginFailedCountReset(String email) throws Exception {
        log.info("loginFailedCountReset email:{}", email);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserLog(UserLogVo userLog) throws Exception {
        notNullCheck(userLog);
        log.info("insertUserLog userId:{}", userLog.getUserId());
        return commonSynchronByUserId(userLog.getUserId());
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserIp(String email, String ipAddress) throws Exception {
        log.info("inserUserIp email:{}, ipAddress:{}", email, ipAddress);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> deleteUserIp(String email, String ipAddress) throws Exception {
        log.info("deleteUserIp email:{}, ipAddress:{}", email, ipAddress);
        return commonSynchronByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUser(UserVo user) throws Exception {
        notNullCheck(user);
        log.info("insertUser userId:{}", user.getUserId());
        return commonSynchronByUserId(user.getUserId());
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserSecurity(UserSecurityVo userSecurity) throws Exception {
        notNullCheck(userSecurity);
        log.info("insertUserSecurity userId:{}", userSecurity.getUserId());
        return commonSynchronByUserId(userSecurity.getUserId());
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserData(UserDataVo userData) throws Exception {
        notNullCheck(userData);
        log.info("insertUserData userId:{}", userData.getUserId());
        return commonSynchronByUserId(userData.getUserId());
    }

    private void notNullCheck(Object object) {
        if (ObjectUtils.isEmpty(object)) {
            throw new BusinessException(GeneralCode.SYS_BODY_NULL);
        }
    }

    private APIResponse<DataMigrationUserIdResponse> commonSynchronByEmail(final String email) throws Exception {
        notNullCheck(email);
        final User user = this.userMapper.queryByEmail(email);
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return commonSynchronByUserId(user.getUserId());
    }

    private APIResponse<DataMigrationUserIdResponse> commonSynchronByUserId(final Long userId) throws Exception {
        notNullCheck(userId);
        DataMigrationUserIdRequest requestBody = new DataMigrationUserIdRequest();
        requestBody.setUserId(userId);
        APIRequest<DataMigrationUserIdRequest> apiRequest = new APIRequest<>();
        apiRequest.setBody(requestBody);
        return null;
    }
}
