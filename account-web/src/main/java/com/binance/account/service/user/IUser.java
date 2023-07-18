package com.binance.account.service.user;

import com.binance.account.vo.security.request.CountAgentNumberRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.MarginRelationShipRequest;
import com.binance.account.vo.security.request.ReCaptchaReq;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.UserIpLikeVo;
import com.binance.account.vo.user.UserIpVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import com.binance.account.vo.user.request.AccountActiveUserRequest;
import com.binance.account.vo.user.request.AccountActiveUserV2Request;
import com.binance.account.vo.user.request.AccountForgotPasswordRequest;
import com.binance.account.vo.user.request.AccountResetPasswordRequest;
import com.binance.account.vo.user.request.AccountResetPasswordVerifyRequest;
import com.binance.account.vo.user.request.BaseDetailRequest;
import com.binance.account.vo.user.request.DeleteUserRequest;
import com.binance.account.vo.user.request.FuzzyMatchUserIndexRequest;
import com.binance.account.vo.user.request.FuzzyMatchUserInfoRequest;
import com.binance.account.vo.user.request.GetAccountIdByRootUserIdRequest;
import com.binance.account.vo.user.request.GetBatchUserTypeListRequest;
import com.binance.account.vo.user.request.GetReferralEmailRequest;
import com.binance.account.vo.user.request.GetUserAgentConfigRequest;
import com.binance.account.vo.user.request.GetUserAgentDetailRequest;
import com.binance.account.vo.user.request.GetUserIdByTradingAccountRequest;
import com.binance.account.vo.user.request.GetUserIdListRequest;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.request.GetUserRequest;
import com.binance.account.vo.user.request.LoginUserRequest;
import com.binance.account.vo.user.request.LoginUserRequestV2;
import com.binance.account.vo.user.request.ModifyUserEmailRequest;
import com.binance.account.vo.user.request.ModifyUserRequest;
import com.binance.account.vo.user.request.OneButtonRegisterRequest;
import com.binance.account.vo.user.request.OneButtonUserAccountActiveRequest;
import com.binance.account.vo.user.request.OrderConfrimStatusRequest;
import com.binance.account.vo.user.request.PasswordVerifyRequest;
import com.binance.account.vo.user.request.RegisterUserRequest;
import com.binance.account.vo.user.request.RegisterUserRequestV2;
import com.binance.account.vo.user.request.ResendSendActiveCodeRequest;
import com.binance.account.vo.user.request.SearchUserListRequest;
import com.binance.account.vo.user.request.SendSmsAuthCodeV2Request;
import com.binance.account.vo.user.request.SendSmsAuthCoderRequest;
import com.binance.account.vo.user.request.SnapshotShareConfigReq;
import com.binance.account.vo.user.request.ThirdPartyUserRegisterRequest;
import com.binance.account.vo.user.request.UpdateAccountUserRequest;
import com.binance.account.vo.user.request.UpdateAgentRateReq;
import com.binance.account.vo.user.request.UpdateNickNameRequest;
import com.binance.account.vo.user.request.UpdatePwdUserRequest;
import com.binance.account.vo.user.request.UpdatePwdUserV2Request;
import com.binance.account.vo.user.request.UpdateUserByEmailRequest;
import com.binance.account.vo.user.request.UserAgentConfigReq;
import com.binance.account.vo.user.request.UserAgentLinkReq;
import com.binance.account.vo.user.request.UserAgentRateReq;
import com.binance.account.vo.user.request.UserAgentSelectShareReq;
import com.binance.account.vo.user.request.UserIpRequest;
import com.binance.account.vo.user.request.UserMobileRequest;
import com.binance.account.vo.user.request.UserRegistRequest;
import com.binance.account.vo.user.request.UserStatusRequest;
import com.binance.account.vo.user.response.AccountActiveUserResponse;
import com.binance.account.vo.user.response.AccountActiveUserV2Response;
import com.binance.account.vo.user.response.AccountForgotPasswordResponse;
import com.binance.account.vo.user.response.AccountResetPasswordResponse;
import com.binance.account.vo.user.response.AccountResetPasswordVerifyResponse;
import com.binance.account.vo.user.response.BaseDetailResponse;
import com.binance.account.vo.user.response.CreateFiatUserResponse;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.account.vo.user.response.FinanceFlagResponse;
import com.binance.account.vo.user.response.FuzzyMatchUserIndexResponse;
import com.binance.account.vo.user.response.FuzzyMatchUserInfoResponse;
import com.binance.account.vo.user.response.GetBatchUserTypeListResponse;
import com.binance.account.vo.user.response.GetUserAgentConfigResponse;
import com.binance.account.vo.user.response.GetUserAgentStatResponse;
import com.binance.account.vo.user.response.GetUserCommissionDetailResponse;
import com.binance.account.vo.user.response.GetUserEmailResponse;
import com.binance.account.vo.user.response.GetUserEmailsResponse;
import com.binance.account.vo.user.response.GetUserListResponse;
import com.binance.account.vo.user.response.GetUserResponse;
import com.binance.account.vo.user.response.KycValidateResponse;
import com.binance.account.vo.user.response.LoginUserResponse;
import com.binance.account.vo.user.response.LoginUserResponseV2;
import com.binance.account.vo.user.response.OneButtonRegisterResponse;
import com.binance.account.vo.user.response.RegisterUserResponse;
import com.binance.account.vo.user.response.RegisterUserResponseV2;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import com.binance.account.vo.user.response.SearchUserListResponse;
import com.binance.account.vo.user.response.SendSmsAuthCodeV2Response;
import com.binance.account.vo.user.response.SendSmsAuthCoderResponse;
import com.binance.account.vo.user.response.SignLVTStatusResponse;
import com.binance.account.vo.user.response.SnapshotShareConfigRes;
import com.binance.account.vo.user.response.SnapshotShareConfigsRes;
import com.binance.account.vo.user.response.SpecialUserIdResponse;
import com.binance.account.vo.user.response.UpdateAccountUserResponse;
import com.binance.account.vo.user.response.UpdatePwdUserResponse;
import com.binance.account.vo.user.response.UpdatePwdUserV2Response;
import com.binance.account.vo.user.response.UserAgentRateResponse;
import com.binance.account.vo.user.response.UserBriefInfoResponse;
import com.binance.account.vo.user.response.UserTypeResponse;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import com.binance.master.commons.SearchResult;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.List;

public interface IUser {

    /**
     * 用户注册
     *
     * @param user
     * @return
     * @throws Exception
     */
    APIResponse<RegisterUserResponse> register(APIRequest<RegisterUserRequest> request) throws Exception;


    /**
     * 用户注册 V2
     *
     * @return
     * @throws Exception
     */
    APIResponse<RegisterUserResponseV2> registerV2(APIRequest<RegisterUserRequestV2> request) throws Exception;


    /**
     * 登录
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<LoginUserResponse> login(APIRequest<LoginUserRequest> request) throws Exception;


    /**
     * 登录V2
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<LoginUserResponseV2> loginV2(APIRequest<LoginUserRequestV2> request) throws Exception;

    /**
     * 修改密码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UpdatePwdUserResponse> updatePwd(APIRequest<UpdatePwdUserRequest> request) throws Exception;

    /**
     * 修改密码（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UpdatePwdUserV2Response> updatePwdV2(APIRequest<UpdatePwdUserV2Request> request) throws Exception;

    /**
     * 修改账号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UpdateAccountUserResponse> updateAccount(APIRequest<UpdateAccountUserRequest> request) throws Exception;

    /**
     * 账号激活
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountActiveUserResponse> accountActive(APIRequest<AccountActiveUserRequest> request) throws Exception;

    /**
     * 账户激活V2
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountActiveUserV2Response> accountActiveV2(APIRequest<AccountActiveUserV2Request> request) throws Exception;

    /**
     * 仅供内部使用-账号激活
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountActiveUserResponse> internalAccountActive(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 根据Email获取用户信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<GetUserResponse> getUserByEmail(APIRequest<GetUserRequest> request) throws Exception;

    /**
     * 根据UserID获取用户信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<GetUserResponse> getUserById(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 根据UserID获取用户基本信息
     *
     * @param request
     * @return UserVo only
     * @throws Exception
     */
    APIResponse<GetUserResponse> checkAndGetUserById(APIRequest<com.binance.account.vo.user.request.UserIdRequest> request) throws Exception;

    /**
     * 忘记密码 发送邮件
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountForgotPasswordResponse> forgotPasswordSendEmail(APIRequest<AccountForgotPasswordRequest> request)
            throws Exception;

    /**
     * 重置密码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountResetPasswordResponse> resetPassword(APIRequest<AccountResetPasswordRequest> request)
            throws Exception;

    /**
     * 启用用户状态
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> enableUserStatus(APIRequest<UserStatusRequest> request) throws Exception;

    /**
     * 停用用户状态
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> disableUserStatus(APIRequest<UserStatusRequest> request) throws Exception;

    /**
     * 根据Email更新用户信息
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateUserByEmail(APIRequest<UpdateUserByEmailRequest> request);

    /**
     * 重新发送激活码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<ResendSendActiveCodeResponse> resendSendActiveCode(APIRequest<ResendSendActiveCodeRequest> request)
            throws Exception;

    /**
     * 发送短信验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SendSmsAuthCoderResponse> sendSmsAuthCode(APIRequest<SendSmsAuthCoderRequest> request) throws Exception;

    /**
     * 发送短信验证码（根据场景发送对应模版短信）
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SendSmsAuthCodeV2Response> sendSmsAuthCodeV2(APIRequest<SendSmsAuthCodeV2Request> request) throws Exception;

    /**
     * 根据用户Id查询用户邮箱
     *
     * @param request
     * @throws Exception
     */
    APIResponse<GetUserEmailResponse> getUserEmailByUserId(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 根据用户Id查询用户邮箱（批量）
     *
     * @param request
     * @throws Exception
     */
    APIResponse<GetUserEmailsResponse> getUserEmailByUserIds(APIRequest<GetUserListRequest> request) throws Exception;
    
    /**
     * 根据用户email查询用户id（批量）
     *
     * @param request
     * @throws Exception
     */
    APIResponse<GetUserEmailsResponse> getUserIdsByEmails(APIRequest<GetUserIdListRequest> request) throws Exception;

    /**
     * 密码验证
     *
     * @param request
     * @throws Exception
     */
    APIResponse<Boolean> passwordCheck(APIRequest<PasswordVerifyRequest> request) throws Exception;

    /**
     * 搜索用户
     *
     * @param request
     * @return
     */
    APIResponse<SearchUserListResponse> searchUserList(APIRequest<SearchUserListRequest> request);

    /**
     * 基础用户信息
     *
     * @param request
     * @return
     */
    APIResponse<BaseDetailResponse> baseDetail(APIRequest<BaseDetailRequest> request)throws Exception;

    /**
     * 摘要用户信息
     *
     * @param request
     * @return
     */
    APIResponse<UserBriefInfoResponse> briefInfo(APIRequest<UserIdReq> request);

    /**
     * 根据email获取用户id
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Long> getUserIdByEmail(APIRequest<GetUserRequest> request) throws Exception;

    /**
     * 根据tradingAccount获取userId
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Long> getUserIdByTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception;

    /**
     * 更具userId列表批量获取用户信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<GetUserListResponse> getUserListByUserIds(APIRequest<GetUserListRequest> request) throws Exception;

    /**
     * APP忘记密码-发送验证码邮件
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountForgotPasswordResponse> sendAppForgotPasswordEmail(
            APIRequest<AccountForgotPasswordRequest> request) throws Exception;


    /**
     * 修改用户邮箱
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> modifyUserEmail(APIRequest<ModifyUserEmailRequest> request) throws Exception;

    /**
     * APP重置密码验证码校验
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountResetPasswordVerifyResponse> resetAppPasswordVerify(
            APIRequest<AccountResetPasswordVerifyRequest> request) throws Exception;

    /**
     * APP重置密码
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountResetPasswordResponse> resetAppPassword(APIRequest<AccountResetPasswordRequest> request)
            throws Exception;

    /**
     * 修改用户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> modifyUser(APIRequest<ModifyUserRequest> request) throws Exception;

    /**
     * 修改用户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> updateNickName(APIRequest<UpdateNickNameRequest> request) throws Exception;

    /**
     * 删除用户
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> deleteUser(APIRequest<DeleteUserRequest> request);

    /**
     * 检查邮箱是否占用
     * 
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> isExist(APIRequest<String> request) throws Exception;

    APIResponse<Boolean> ipIsExist(APIRequest<UserIpRequest> request);

    /**
     * 用户kyc,地址认证信息
     *
     * @param request
     * @return
     */
    APIResponse<List<KycValidateResponse>> kycValidateInfo(APIRequest<BaseDetailRequest> request);

    /**
     * 统计用户注册量
     * 
     * @param request
     * @return
     */
    APIResponse<Integer> todayRegist(APIRequest<UserRegistRequest> request);

    /**
     * 记录用户ip
     * 
     * @param request
     * @return
     */
    APIResponse<Integer> saveUserIp(APIRequest<UserIpRequest> request);

    /**
     * 根据手机号获取用户信息
     * 
     * @param request
     * @return
     */
    APIResponse<GetUserResponse> getUserByMobile(APIRequest<UserMobileRequest> request);

    /**
     * 去除最后一段IP进行检查IP是否能匹配
     * 
     * @param request
     * @return
     */
    APIResponse<UserIpLikeVo> ipLikeCheck(APIRequest<UserIpLikeVo> request);

    /**
     * 获取用户的IP信息
     * @param userId
     * @return
     */
    List<UserIpVo> getUserIpList(Long userId);

    /**
     * 修复越南用户手机号的数据
     * 
     * @param request
     * @return
     */
    APIResponse<Boolean> fixVNUser(APIRequest<UserIdRequest> request);

    /**
     * 根据UserId获取用户状态
     *
     * @param request
     * @return
     */
    APIResponse<UserStatusEx> getUserStatusByUserId(APIRequest<UserIdRequest> request);

    /**
     * 保存recaptcha响应结果
     * @param request
     * @return
     */
    APIResponse<Integer> saveRecaptcha(APIRequest<ReCaptchaReq> request);

    /**
     * 统计某个推荐人的推荐用户数量
     * @param request
     * @return
     */
    APIResponse<Long> countAgentNumber(APIRequest<CountAgentNumberRequest> request) throws Exception;


    /**
     * 根据UserID获取用户信息创建margin账户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CreateMarginUserResponse> createMarginAccount(APIRequest<CreateMarginAccountRequest> request) throws Exception;

    /**
     * 根据UserID获取用户信息创建fiat账户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CreateFiatUserResponse> createFiatAccount(APIRequest<UserIdRequest> request, boolean isChinaKyc)
            throws Exception;


    /**
     * 判断两个userid是否是margin关系
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> isMarginRelationShip( APIRequest<MarginRelationShipRequest> request) throws Exception;

    /**
     * 根据useridlist获取账号类型信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<List<GetBatchUserTypeListResponse>> getBatchUserTypeList(APIRequest<GetBatchUserTypeListRequest> request) throws Exception;


    /**
     * 根据主账户userid来获取相应类型的accountid
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Long> getAccountIdByRootUserId(APIRequest<GetAccountIdByRootUserIdRequest> request) throws Exception;




    //获取推荐人详情
    APIResponse<List<GetUserResponse>> getUserAgentDetail(APIRequest<GetUserAgentDetailRequest> request) throws Exception;

    //模糊匹配用户index信息
    APIResponse<FuzzyMatchUserIndexResponse> fuzzyMatchUserIndex(APIRequest<FuzzyMatchUserIndexRequest> request) throws Exception;

    //模糊匹配用户info信息
    APIResponse<FuzzyMatchUserInfoResponse> fuzzyMatchUserInfo(APIRequest<FuzzyMatchUserInfoRequest> request) throws Exception;

    //用户返佣详情信息
    APIResponse<GetUserCommissionDetailResponse> getUserCommissionDetail(APIRequest<GetUserAgentDetailRequest> request) throws Exception ;


    APIResponse<SpecialUserIdResponse> getSpecialUserIds() throws Exception;

    APIResponse<UserTypeResponse> getUserTypeByUserId(APIRequest<UserIdReq> request) throws Exception;


    Long checkTestnetEmailIfPassKyc(String email) throws Exception;

    /**
     * 更新用户下单确认状态
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> updateOrderConfirmStatus(APIRequest<OrderConfrimStatusRequest> request) throws Exception;

    APIResponse<String> createUserAgentRate(APIRequest<UserAgentRateReq> request) throws Exception;

    APIResponse<UserAgentRateResponse> getUserAgentRateByCode(APIRequest<String> request) throws Exception;

    APIResponse<SearchResult<GetUserAgentStatResponse>> getUserPromoteByUserId(APIRequest<UserAgentLinkReq> request)
            throws Exception;

    APIResponse<SearchResult<String>> getReferralUserEmail(APIRequest<GetReferralEmailRequest> request)
            throws Exception;

    APIResponse<GetUserAgentStatResponse> getOldPromoteByUserId(APIRequest<Long> request) throws Exception;

    APIResponse updateLabelByAgentCode(APIRequest<UpdateAgentRateReq> request) throws Exception;

    APIResponse<Integer> getRemainingAgentLinkNum(APIRequest<Long> request) throws Exception;

    APIResponse<Boolean> saveOrupdateUserAgentConfig(APIRequest<UserAgentConfigReq> request) throws Exception;

    APIResponse<SearchResult<GetUserAgentConfigResponse>> selectUserAgentConfig(
            APIRequest<GetUserAgentConfigRequest> request) throws Exception;

    APIResponse<UserStatusEx> getUserStatusByAgentCode(APIRequest<String> request)throws Exception;

    APIResponse<Boolean> selectOneAsShareCode(APIRequest<UserAgentSelectShareReq> request) throws Exception;

    APIResponse<Boolean> saveOrupdateSnapshotShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception;

    APIResponse<Boolean> deleteSnapshotShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception;

    APIResponse<SnapshotShareConfigsRes> selectAllSnapShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception;

    APIResponse<GetUserAgentStatResponse> selectCheckedShareCodeByUserId(APIRequest<Long> request) throws Exception;

    APIResponse<List<SnapshotShareConfigRes>> selectAllSnapShareConfigForPnk(APIRequest<SnapshotShareConfigReq> request)throws Exception;

    /**
     * 统计某个推荐人的推荐用户数量
     * @param request
     * @return
     */
    APIResponse<Long> countRealUserAgentNumber(APIRequest<CountAgentNumberRequest> request) throws Exception;

    Integer addOrUpdateUserConfig(Long userId, String configType, String configName) throws Exception ;


    String getConfigByConfigType(Long userId,String configType);
    
    /**
     * 第三方机构的用户在binance注册
     * 无密码注册，生成随机密码，发送密码重置邮件
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<RegisterUserResponse> thirdPartyUserRegister(APIRequest<ThirdPartyUserRegisterRequest> request) throws Exception;

    /**
     * 第三方机构的用户发送密码重置邮件
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<AccountForgotPasswordResponse> thirdPartySendResetPasswordEmail(APIRequest<AccountForgotPasswordRequest> request)
            throws Exception;
    
    APIResponse<OneButtonRegisterResponse> oneButtonRegister(APIRequest<OneButtonRegisterRequest> request) throws Exception;
    
    APIResponse<AccountActiveUserV2Response> oneButtonUserAccountActive(APIRequest<OneButtonUserAccountActiveRequest> request) throws Exception;

    APIResponse<Void> oneButtonUserForgotPassword(APIRequest<UserIdRequest> request) throws Exception;

    APIResponse<FinanceFlagResponse> financeFlag(APIRequest<UserIdReq> request) throws Exception;

    APIResponse<SearchResult<SelectUserAgentLogResponse>> selectMiningUserAgentLog(APIRequest<SelectMiningAgentLogRequest> request)throws Exception;

    APIResponse<Long> selectMiningUserAgentNum(APIRequest<SelectUserAgentNumRequest> request)throws Exception;

    APIResponse<String> createMiningAgentRate(APIRequest<Long> request)throws Exception;


    APIResponse<List<UserVo>> getUserStatusByUserIds(APIRequest<GetUserListRequest> request);

    APIResponse<Boolean> finishLVTSAQ(APIRequest<UserIdReq> request);

    APIResponse<UserSAQResponse> queryLVTSAQStatus(APIRequest<UserIdReq> request);
}
