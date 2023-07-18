package com.binance.account.controller.sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.sso.AccountApi;
import com.binance.account.service.sso.ISso;
import com.binance.account.vo.datamigration.response.DataMigrationUserIdResponse;
import com.binance.account.vo.sso.PasswordLogVo;
import com.binance.account.vo.sso.UserDataVo;
import com.binance.account.vo.sso.UserLogVo;
import com.binance.account.vo.sso.UserSecurityVo;
import com.binance.account.vo.sso.UserVo;
import com.binance.master.models.APIResponse;

/**
 * Created by Fei.Huang on 2018/4/11.
 */
@RestController
public class SsoController implements AccountApi {

    @Autowired
    private ISso iSso;

    @Override
    public APIResponse<DataMigrationUserIdResponse> activationEmail(@RequestParam("email") String email)
            throws Exception {
        return iSso.activationEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> forbiddenEmail(@RequestParam("email") String email)
            throws Exception {
        return iSso.forbiddenEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> forbiddenTradeByEmail(@RequestParam("email") String email)
            throws Exception {
        return iSso.forbiddenTradeByEmail(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> updateUserPassword(@RequestParam UserVo user) throws Exception {
        return iSso.updateUserPassword(user);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> resetPayPwdFailNumAndPasswordFailNum(
            @RequestParam("userId") Long userId) throws Exception {
        return iSso.resetPayPwdFailNumAndPasswordFailNum(userId);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertPasswordLog(@RequestBody PasswordLogVo passwordLog)
            throws Exception {
        return iSso.insertPasswordLog(passwordLog);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> loginFailedCountIncrement(@RequestParam("email") String email)
            throws Exception {
        return iSso.loginFailedCountIncrement(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> loginFailedCountReset(@RequestParam("email") String email)
            throws Exception {
        return iSso.loginFailedCountReset(email);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserLog(@RequestBody UserLogVo userLog) throws Exception {
        return iSso.insertUserLog(userLog);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserIp(@RequestParam("email") String email,
                                                                 @RequestParam("ipAddress") String ipAddress) throws Exception {
        return iSso.insertUserIp(email, ipAddress);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> deleteUserIp(@RequestParam("email") String email,
                                                                 @RequestParam("ipAddress") String ipAddress) throws Exception {
        return iSso.deleteUserIp(email, ipAddress);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUser(@RequestBody UserVo user) throws Exception {
        return iSso.insertUser(user);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserSecurity(@RequestBody UserSecurityVo userSecurity)
            throws Exception {
        return iSso.insertUserSecurity(userSecurity);
    }

    @Override
    public APIResponse<DataMigrationUserIdResponse> insertUserData(@RequestBody UserDataVo userData) throws Exception {
        return iSso.insertUserData(userData);
    }
}
