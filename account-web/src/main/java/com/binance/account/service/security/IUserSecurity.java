package com.binance.account.service.security;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.account.data.entity.security.VerificationsTwo;
import com.binance.account.data.entity.user.User;
import com.binance.account.service.security.model.MultiFactorSceneCheckQuery;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.request.AccountResetPasswordRequestV2;
import com.binance.account.vo.security.request.ActiveLoginRequest;
import com.binance.account.vo.security.request.BatchUpdateSecurityLevelRequest;
import com.binance.account.vo.security.request.BindEmailRequest;
import com.binance.account.vo.security.request.BindGoogleVerifyRequest;
import com.binance.account.vo.security.request.BindGoogleVerifyV2Request;
import com.binance.account.vo.security.request.BindMobileRequest;
import com.binance.account.vo.security.request.BindMobileV2Request;
import com.binance.account.vo.security.request.BindPhishingCodeRequest;
import com.binance.account.vo.security.request.BindPhishingCodeV2Request;
import com.binance.account.vo.security.request.ChangeEmailRequest;
import com.binance.account.vo.security.request.ChangeMobileRequest;
import com.binance.account.vo.security.request.CloseWithdrawWhiteStatusV2Request;
import com.binance.account.vo.security.request.ConfirmCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.DisableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.DisableFundPasswordRequest;
import com.binance.account.vo.security.request.DisableLoginRequest;
import com.binance.account.vo.security.request.EnableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.EnableFundPasswordRequest;
import com.binance.account.vo.security.request.GetCapitalWithdrawVerifyParamRequest;
import com.binance.account.vo.security.request.GetUserEmailAndMobileByUserIdRequest;
import com.binance.account.vo.security.request.GetUserIdByEmailOrMobileRequest;
import com.binance.account.vo.security.request.MobileRateLimitRequest;
import com.binance.account.vo.security.request.MobileRequest;
import com.binance.account.vo.security.request.OneButtonActivationRequest;
import com.binance.account.vo.security.request.OneButtonDisableRequest;
import com.binance.account.vo.security.request.OpenOrCloseBNBFeeRequest;
import com.binance.account.vo.security.request.OpenOrCloseMobileVerifyRequest;
import com.binance.account.vo.security.request.OpenOrCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.OpenWithdrawWhiteStatusV2Request;
import com.binance.account.vo.security.request.ResetActivationUserRequest;
import com.binance.account.vo.security.request.ResetFundPasswordRequest;
import com.binance.account.vo.security.request.ResetGoogleRequest;
import com.binance.account.vo.security.request.ResetSecurityRequest;
import com.binance.account.vo.security.request.SecurityLevelSettingRequest;
import com.binance.account.vo.security.request.SecurityStatusRequest;
import com.binance.account.vo.security.request.SendBindEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.SendBindMobileVerifyCodeRequest;
import com.binance.account.vo.security.request.SendEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.UnbindGoogleRequest;
import com.binance.account.vo.security.request.UnbindGoogleV2Request;
import com.binance.account.vo.security.request.UnbindMobileRequest;
import com.binance.account.vo.security.request.UnbindMobileV2Request;
import com.binance.account.vo.security.request.UpdateUserSecurityByUserIdRequest;
import com.binance.account.vo.security.request.UpdateWithdrawStatusRequest;
import com.binance.account.vo.security.request.UserEmailRequest;
import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.security.request.VerificationsDemandRequest;
import com.binance.account.vo.security.request.VerifyFundPasswordRequest;
import com.binance.account.vo.security.request.WithdrawFaceStatusChangeRequest;
import com.binance.account.vo.security.response.AccountResetPasswordResponseV2;
import com.binance.account.vo.security.response.AccountUpdateTimeForTrade;
import com.binance.account.vo.security.response.BindEmailResponse;
import com.binance.account.vo.security.response.BindGoogleVerifyResponse;
import com.binance.account.vo.security.response.BindGoogleVerifyV2Response;
import com.binance.account.vo.security.response.ChangeEmailResponse;
import com.binance.account.vo.security.response.ChangeMobileResponse;
import com.binance.account.vo.security.response.CheckForbidCodeResponse;
import com.binance.account.vo.security.response.CloseWithdrawWhiteStatusResponse;
import com.binance.account.vo.security.response.CloseWithdrawWhiteStatusV2Response;
import com.binance.account.vo.security.response.ConfirmCloseWithdrawWhiteStatusResponse;
import com.binance.account.vo.security.response.DisableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.DisableFundPasswordResponse;
import com.binance.account.vo.security.response.EnableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.EnableFundPasswordResponse;
import com.binance.account.vo.security.response.GetUser2faResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetUserEmailAndMobileByUserIdResponse;
import com.binance.account.vo.security.response.GetUserIdByEmailOrMobileResponse;
import com.binance.account.vo.security.response.GoogleAuthKeyResp;
import com.binance.account.vo.security.response.OpenWithdrawWhiteStatusV2Response;
import com.binance.account.vo.security.response.ResetFundPasswordResponse;
import com.binance.account.vo.security.response.SendBindEmailVerifyCodeResponse;
import com.binance.account.vo.security.response.SendBindMobileVerifyCodeResponse;
import com.binance.account.vo.security.response.SendEmailVerifyCodeResponse;
import com.binance.account.vo.security.response.UnbindGoogleVerifyResponse;
import com.binance.account.vo.security.response.UnbindGoogleVerifyV2Response;
import com.binance.account.vo.security.response.UnbindMobileResponse;
import com.binance.account.vo.security.response.UnbindMobileV2Response;
import com.binance.account.vo.security.response.UserRiskInfoResponse;
import com.binance.account.vo.security.response.UserSecurityListResponse;
import com.binance.account.vo.security.response.VerificationsDemandResponse;
import com.binance.account.vo.security.response.VerifyFundPasswordResponse;
import com.binance.account.vo.security.response.WithdrawTimeForTradeResponse;
import com.binance.account.vo.user.request.AccountForgotPasswordPreCheckRequest;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.response.AccountForgotPasswordPreCheckResponse;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface IUserSecurity {

    /**
     * 解绑谷歌验证
     *
     * @param userId
     * @return
     * @throws Exception
     */
    APIResponse<UnbindGoogleVerifyResponse> unbindGoogleVerify(APIRequest<UnbindGoogleRequest> request)
            throws Exception;

    /**
     * 解绑谷歌验证（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UnbindGoogleVerifyV2Response> unbindGoogleVerifyV2(@RequestBody APIRequest<UnbindGoogleV2Request> request)
            throws Exception;

    /**
     * 重置2次验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UnbindGoogleVerifyResponse> resetGoogleVerify(APIRequest<ResetGoogleRequest> request) throws Exception;

    /**
     * 绑定谷歌验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<BindGoogleVerifyResponse> bindGoogleVerify(APIRequest<BindGoogleVerifyRequest> request)
            throws Exception;

    /**
     * 解绑谷歌验证（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<BindGoogleVerifyV2Response> bindGoogleVerifyV2(@RequestBody APIRequest<BindGoogleVerifyV2Request> request)
            throws Exception;

    /**
     * 绑定手机号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> bindMobile(APIRequest<BindMobileRequest> request) throws Exception;

    /**
     * 绑定手机（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> bindMobileV2(@RequestBody APIRequest<BindMobileV2Request> request) throws Exception;

    /**
     * 解绑手机号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UnbindMobileResponse> unbindMobile(APIRequest<UnbindMobileRequest> request) throws Exception;

    /**
     * 解绑手机（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UnbindMobileV2Response> unbindMobileV2(@RequestBody APIRequest<UnbindMobileV2Request> request)
            throws Exception;

    /**
     * 重置登录错误次数
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> resetLoginFailedNum(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 根据用户Id更新用户安全信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> updateUserSecurityByUserId(APIRequest<UpdateUserSecurityByUserIdRequest> request)
            throws Exception;

    /**
     * 根据用户Id获取用户安全信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UserSecurityVo> getUserSecurityByUserId(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 根据用户邮箱获取用户安全信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UserSecurityVo> getUserSecurityByEmail(APIRequest<UserEmailRequest> request) throws Exception;

    /**
     * 判断手机号是否存在
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> isMobileExist(APIRequest<MobileRequest> request) throws Exception;

    /**
     * 判断手机号是否存在
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> isMobileExistRateLimit(APIRequest<MobileRateLimitRequest> request) throws Exception;

    /**
     * 发送绑定手机验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SendBindMobileVerifyCodeResponse> sendBindMobileVerifyCode(
            APIRequest<SendBindMobileVerifyCodeRequest> request) throws Exception;



    /**
     * 发送绑定邮箱验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    SendBindEmailVerifyCodeResponse sendBindEmailVerifyCode(SendBindEmailVerifyCodeRequest request) throws Exception;



    /**
     * 绑定邮箱
     *
     * @param request
     * @return
     * @throws Exception
     */
    BindEmailResponse bindEmail(APIRequest<BindEmailRequest> request) throws Exception;



    /**
     * 发送邮件验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    SendEmailVerifyCodeResponse sendEmailVerifyCode(SendEmailVerifyCodeRequest request) throws Exception;

    /**
     * 打开手机验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> openMobileVerify(APIRequest<OpenOrCloseMobileVerifyRequest> request) throws Exception;

    /**
     * 关闭
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> closeMobileVerify(APIRequest<OpenOrCloseMobileVerifyRequest> request) throws Exception;

    /**
     * 打开手机验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> openBNBFee(APIRequest<OpenOrCloseBNBFeeRequest> request) throws Exception;

    /**
     * 关闭
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> closeBNBFee(APIRequest<OpenOrCloseBNBFeeRequest> request) throws Exception;

    /**
     * 验证谷歌
     *
     * @param userId
     * @param code
     * @return
     * @throws Exception
     */
    void checkGoogleAuthenticator(Long userId, Integer code) throws Exception;


    /**
     * 短信验证
     *
     * @param userId
     * @param code
     * @return
     * @throws Exception
     */
    VerificationsTwo isSmsAuthenticator(Long userId, String code, boolean autoDel, String mobile, String mobileCode);

    /**
     * 谷歌手机yubikey验证统一入口
     *
     * @param userId   用户id
     * @param authType 认证类型
     * @param code     认证码
     * @param autoDel  是否自动删除历史认证代码
     * @return
     * @throws Exception
     */
    VerificationsTwo verificationsTwoV2(Long userId, AuthTypeEnum authType, String code, SecurityKeyApplicationScenario scenario, boolean autoDel)
            throws Exception;


    /**
     * 谷歌手机验证统一入口
     *
     * @param userId   用户id
     * @param authType 认证类型
     * @param code     认证码
     * @param autoDel  是否自动删除历史认证代码
     * @return
     * @throws Exception
     */
    VerificationsTwo verificationsTwo(Long userId, AuthTypeEnum authType, String code, boolean autoDel)
            throws Exception;


    /**
     * 基于场景的多因子校验统一入口
     *
     * @throws Exception
     */
    void verifyMultiFactors(MultiFactorSceneVerify verify) throws Exception;

    /**
     * 获取需要校验的2fa校验列表（包含校验因子类型和校验对象掩码等）
     *
     * @throws Exception
     */
    MultiFactorSceneCheckResult getVerificationTwoCheckList(MultiFactorSceneCheckQuery query) throws Exception;


        /**
         * 开启提币白名单
         *
         * @param request
         * @return
         * @throws Exception
         */
    APIResponse<Integer> openWithdrawWhiteStatus(APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request)
            throws Exception;

    /**
     * 开启提币白名单，支持三码验证方式
     * @param request
     * @return
     */
    APIResponse<OpenWithdrawWhiteStatusV2Response> openWithdrawWhiteStatusV2(@Validated @RequestBody APIRequest<OpenWithdrawWhiteStatusV2Request> request) throws Exception;

    /**
     * 关闭提币白名单
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CloseWithdrawWhiteStatusResponse> closeWithdrawWhiteStatus(
            APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request) throws Exception;

    /**
     * 关闭提币白名单，支持三码验证方式
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<CloseWithdrawWhiteStatusV2Response> closeWithdrawWhiteStatusV2(APIRequest<CloseWithdrawWhiteStatusV2Request> request) throws Exception;

    /**
     * 确定关闭提币白名单
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<ConfirmCloseWithdrawWhiteStatusResponse> confirmCloseWithdrawWhiteStatus(
            APIRequest<ConfirmCloseWithdrawWhiteStatusRequest> request) throws Exception;

    /**
     * 设置(添加或修改)防钓鱼码
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> aouAntiPhishingCode(APIRequest<BindPhishingCodeRequest> request) throws Exception;

    /**
     * 设置(添加或修改)防钓鱼码（新2fa）
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> aouAntiPhishingCodeV2(APIRequest<BindPhishingCodeV2Request> request) throws Exception;
    
    /**
     * 校验forbidCode是否有效
     */
    CheckForbidCodeResponse checkForbidCode(String code);

    /**
     * 锁定用户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> lockUser(APIRequest<UserLockRequest> request) throws Exception;

    /**
     * 把被锁定的用户加入到cache中，方便com.binance.account.job.UnlockUserJobHandler自动解锁
     *
     * @param lockEndTime
     * @param email
     * @return
     */
    Boolean addLockUserCache(Long lockEndTime, String email);

    /**
     * 设置用户安全级别
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> setSecurityLevel(APIRequest<SecurityLevelSettingRequest> request) throws Exception;

    /**
     * 批量设置用户安全级别
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<List<String>> batchUpdateSecurityUserLevel(BatchUpdateSecurityLevelRequest request) throws Exception;

    /**
     * 重置用户二次验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> resetSecurity(APIRequest<ResetSecurityRequest> request) throws Exception;

    /**
     * 重置用户二次验证，需要手动输入IP和terminal, 防止线程调用登记安全变更日志时获取不到IP问题
     *
     * @param user
     * @param resetType
     * @param ip
     * @param terminal
     * @return
     */
    Integer resetSecurity(User user, ResetSecurityRequest.ResetType resetType, String ip, TerminalEnum terminal);

    /**
     * 一键启用用户
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> oneButtonActivation(APIRequest<OneButtonActivationRequest> request) throws Exception;

    /**
     * 一键启用用户，用于服务内调用，排除了线程调用获取不到IP的问题
     *
     * @param user
     * @param ip
     * @param terminal
     */
    void oneButtonActivation(User user, String ip, TerminalEnum terminal);

    /**
     * 一键启用禁用
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> oneButtonDisable(APIRequest<OneButtonDisableRequest> request) throws Exception;

    /**
     * 风控禁用交易以及撤单
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> disableTradeAndCancleOrder(APIRequest<OneButtonDisableRequest> request) throws Exception;

    /**
     * 新版版的重置解禁流程成功后启动用户
     *
     * @param request
     */
    void resetActivationUser(ResetActivationUserRequest request);

    /**
     * 后台禁用登录
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> disableLogin(APIRequest<DisableLoginRequest> request) throws Exception;

    /**
     * 后台启动用登录
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> activeLogin(APIRequest<ActiveLoginRequest> request) throws Exception;

    /**
     * 根据用户id查防钓鱼码
     *
     * @param request
     * @return
     */
    APIResponse<String> selectAntiPhishingCode(APIRequest<UserIdRequest> request) throws Exception;

    /**
     * 批量查用户手机号等信息
     *
     * @param request
     * @return
     */
    APIResponse<UserSecurityListResponse> selectUserSecurityList(APIRequest<GetUserListRequest> request);

    /**
     * 修改禁用状态
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateStatusByUserId(APIRequest<SecurityStatusRequest> request);

    /**
     * xin修改禁用状态
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateWithdrawStatusByUserId(APIRequest<UpdateWithdrawStatusRequest> request);

    /**
     * 生成一个账户禁用码，有效期24小时
     */
    String generateDisableCode(Long userId);

    /**
     * 修改用户密码
     *
     * @param email
     * @param salt
     * @param encodedPassword
     * @param riskEngineResult
     * @return
     */
    int updateUserPassword(String email, String salt, String encodedPassword, boolean riskEngineResult, String rediskey);



    /**
     * 修改用户密码
     *
     * @param email
     * @param salt
     * @param encodedPassword
     * @param riskEngineResult
     * @return
     */
    int updateUserPasswordAndSafePwd(String email, String salt, String encodedPassword, boolean riskEngineResult, String rediskey,String safePassword)throws Exception;

    /**
     * 获取用户密码修改时间(默认缓存24小时)
     *
     * @param userId
     * @return
     */
    Long getPasswordUpdateTime(Long userId);

    /**
     * 生成gAuth密钥
     *
     * @param requestBody
     * @return
     */
    GoogleAuthKeyResp generateAuthKeyAndQrCode(UserIdRequest requestBody);

    /**
     * 获取用户2FA解绑时间
     *
     * @param userId
     * @return
     */
    Long get2FaUnbindTime(Long userId);


    /**
     * 设置margin账号是否使用燃烧bnb的操作
     */
    Integer setMarginBnbFee(Long rootUserId, Boolean enableBnbFlag) throws Exception;

    Boolean getReBindGoogleVerifyStatus(Long userId);

    AccountUpdateTimeForTrade getAccountUpdateTimeForTrade(Long userId);
    /**
     * 判断当前场景下是否需要Yubikey验证
     * @param scenario
     * @param userId
     * @return
     */
    boolean isYubikeyEnabledInSpecifiedScenario(Long userId, SecurityKeyApplicationScenario scenario);

    void updateYubikeyEnableScenarios(Long userId, Map<SecurityKeyApplicationScenario, Boolean> scenarios, String code);

    /**
     * 一键禁用(fiat)
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<?> disableForFiat(APIRequest<UserIdRequest> request) throws Exception;

    WithdrawTimeForTradeResponse getWithdrawTimeForTrade(Long userId) throws Exception;

    EnableFastWithdrawSwitchResponse enableFastWithdrawSwitch(EnableFastWithdrawSwitchRequest request) throws Exception;

    DisableFastWithdrawSwitchResponse disableFastWithdrawSwitch(DisableFastWithdrawSwitchRequest request) throws Exception;


    Integer setIsolatedMarginBnbFee(Long isolatedMarginUserId, Boolean enableBnbFlag) throws Exception;



    EnableFundPasswordResponse enableFundPassword(EnableFundPasswordRequest request) throws Exception;

    DisableFundPasswordResponse disableFundPassword(DisableFundPasswordRequest request) throws Exception;

    ResetFundPasswordResponse resetFundPassword(ResetFundPasswordRequest request) throws Exception;

    VerifyFundPasswordResponse verifyFundPassword(VerifyFundPasswordRequest request) throws Exception;





    GetUserEmailAndMobileByUserIdResponse getUserEmailAndMobileByUserId(GetUserEmailAndMobileByUserIdRequest request) throws Exception;


    GetUserIdByEmailOrMobileResponse getUserIdByMobileOrEmail(GetUserIdByEmailOrMobileRequest request) throws Exception;


    AccountForgotPasswordPreCheckResponse forgotPasswordPreCheck(APIRequest<AccountForgotPasswordPreCheckRequest> request) throws Exception;



    AccountResetPasswordResponseV2 resetPasswordV2(AccountResetPasswordRequestV2 request) throws Exception;


    GetUser2faResponse getUser2fa(UserIdRequest body) throws Exception;
    /**
     * 验证码中，只要不为空的验证码，就进行验证
     *
     * @param request
     * @return
     * @throws Exception
     */
    VerificationsDemandResponse verificationsDemand(VerificationsDemandRequest request) throws Exception;


    UserRiskInfoResponse getUserRiskInfo(Long userId);

    Integer changeWithdrawFaceCheckStatus(WithdrawFaceStatusChangeRequest request);


    ChangeEmailResponse changeEmail(ChangeEmailRequest request);


    ChangeMobileResponse changeMobile(ChangeMobileRequest request);


    GetCapitalWithdrawVerifyParamResponse getCapitalWithdrawVerfiyParam(GetCapitalWithdrawVerifyParamRequest request);




}
