package com.binance.account.service.sso;


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
public interface ISso {

    APIResponse<DataMigrationUserIdResponse> activationEmail(String email) throws Exception;

    APIResponse<DataMigrationUserIdResponse> forbiddenEmail(String email) throws Exception;

    APIResponse<DataMigrationUserIdResponse> forbiddenTradeByEmail(String email) throws Exception;

    APIResponse<DataMigrationUserIdResponse> updateUserPassword(UserVo user) throws Exception;

    APIResponse<DataMigrationUserIdResponse> resetPayPwdFailNumAndPasswordFailNum(Long userId) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertPasswordLog(PasswordLogVo passwordLog) throws Exception;

    APIResponse<DataMigrationUserIdResponse> loginFailedCountIncrement(String email) throws Exception;

    APIResponse<DataMigrationUserIdResponse> loginFailedCountReset(String email) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertUserLog(UserLogVo userLog) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertUserIp(String email, String ipAddress) throws Exception;

    APIResponse<DataMigrationUserIdResponse> deleteUserIp(String email, String ipAddress) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertUser(UserVo user) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertUserSecurity(UserSecurityVo userSecurity) throws Exception;

    APIResponse<DataMigrationUserIdResponse> insertUserData(UserDataVo userData) throws Exception;
}
