package com.binance.account.api;

import com.binance.account.vo.security.OldWithdrawDailyLimitModifyVo;
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
import com.binance.account.vo.security.request.ConfirmedUserIpChangeRequest;
import com.binance.account.vo.security.request.DisableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.DisableFundPasswordRequest;
import com.binance.account.vo.security.request.DisableLoginRequest;
import com.binance.account.vo.security.request.EnableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.EnableFundPasswordRequest;
import com.binance.account.vo.security.request.GetCapitalWithdrawVerifyParamRequest;
import com.binance.account.vo.security.request.GetReBindGoogleVerifyStatusRequest;
import com.binance.account.vo.security.request.GetUserEmailAndMobileByUserIdRequest;
import com.binance.account.vo.security.request.GetUserIdByEmailOrMobileRequest;
import com.binance.account.vo.security.request.GetVerificationTwoCheckListRequest;
import com.binance.account.vo.security.request.IsSecurityKeyEnabledRequest;
import com.binance.account.vo.security.request.MobileRateLimitRequest;
import com.binance.account.vo.security.request.MobileRequest;
import com.binance.account.vo.security.request.MultiFactorSceneCheckRequest;
import com.binance.account.vo.security.request.OneButtonActivationRequest;
import com.binance.account.vo.security.request.OneButtonDisableRequest;
import com.binance.account.vo.security.request.OpenOrCloseBNBFeeRequest;
import com.binance.account.vo.security.request.OpenOrCloseMobileVerifyRequest;
import com.binance.account.vo.security.request.OpenOrCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.OpenWithdrawWhiteStatusV2Request;
import com.binance.account.vo.security.request.ResetActivationUserRequest;
import com.binance.account.vo.security.request.ResetDisableTradingRequest;
import com.binance.account.vo.security.request.ResetFundPasswordRequest;
import com.binance.account.vo.security.request.ResetGoogleRequest;
import com.binance.account.vo.security.request.ResetSecurityRequest;
import com.binance.account.vo.security.request.SecurityFaceStatusRequest;
import com.binance.account.vo.security.request.SecurityLevelSettingRequest;
import com.binance.account.vo.security.request.SecurityStatusRequest;
import com.binance.account.vo.security.request.SendBindEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.SendBindMobileVerifyCodeRequest;
import com.binance.account.vo.security.request.SendEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.UnbindGoogleRequest;
import com.binance.account.vo.security.request.UnbindGoogleV2Request;
import com.binance.account.vo.security.request.UnbindMobileRequest;
import com.binance.account.vo.security.request.UnbindMobileV2Request;
import com.binance.account.vo.security.request.UpdateSecurityKeyApplicationScenarioRequest;
import com.binance.account.vo.security.request.UpdateUserSecurityByUserIdRequest;
import com.binance.account.vo.security.request.UpdateWithdrawStatusRequest;
import com.binance.account.vo.security.request.UserEmailRequest;
import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.security.request.VarificationTwoRequest;
import com.binance.account.vo.security.request.VerificationTwoV2Request;
import com.binance.account.vo.security.request.VerificationTwoV3Request;
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
import com.binance.account.vo.security.response.ConfirmedUserIpChangeResponse;
import com.binance.account.vo.security.response.DisableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.DisableFundPasswordResponse;
import com.binance.account.vo.security.response.EnableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.EnableFundPasswordResponse;
import com.binance.account.vo.security.response.GetUser2faResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetUserEmailAndMobileByUserIdResponse;
import com.binance.account.vo.security.response.GetUserIdByEmailOrMobileResponse;
import com.binance.account.vo.security.response.GetVerificationTwoCheckListResponse;
import com.binance.account.vo.security.response.GoogleAuthKeyResp;
import com.binance.account.vo.security.response.MultiFactorSceneCheckResponse;
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
import com.binance.account.vo.security.response.VerificationTwoV3Response;
import com.binance.account.vo.security.response.VerificationsDemandResponse;
import com.binance.account.vo.security.response.VerifyFundPasswordResponse;
import com.binance.account.vo.security.response.WithdrawTimeForTradeResponse;
import com.binance.account.vo.user.request.AccountForgotPasswordPreCheckRequest;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.response.AccountForgotPasswordPreCheckResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户安全")
@RequestMapping("/userSecurity")
public interface UserSecurityApi {


    /** --- 以下为前台用户接口 --- **/

    @ApiOperation("解绑谷歌验证-前台")
    @PostMapping("/unbindGoogleVerify")
    APIResponse<UnbindGoogleVerifyResponse> unbindGoogleVerify(@RequestBody APIRequest<UnbindGoogleRequest> request)
            throws Exception;

    @ApiOperation("解绑谷歌验证-前台")
    @PostMapping("/unbindGoogleVerifyV2")
    APIResponse<UnbindGoogleVerifyV2Response> unbindGoogleVerifyV2(@RequestBody APIRequest<UnbindGoogleV2Request> request)
            throws Exception;

    @ApiOperation("绑定谷歌验证-前台")
    @PostMapping("/bindGoogleVerify")
    APIResponse<BindGoogleVerifyResponse> bindGoogleVerify(@RequestBody APIRequest<BindGoogleVerifyRequest> request)
            throws Exception;

    @ApiOperation("绑定谷歌验证-前台")
    @PostMapping("/bindGoogleVerifyV2")
    APIResponse<BindGoogleVerifyV2Response> bindGoogleVerifyV2(@RequestBody APIRequest<BindGoogleVerifyV2Request> request)
            throws Exception;

    @ApiOperation("解绑手机-前台")
    @PostMapping("/unbindMobile")
    APIResponse<UnbindMobileResponse> unbindMobile(@RequestBody APIRequest<UnbindMobileRequest> request)
            throws Exception;

    @ApiOperation("解绑手机-前台")
    @PostMapping("/unbindMobileV2")
    APIResponse<UnbindMobileV2Response> unbindMobileV2(@RequestBody APIRequest<UnbindMobileV2Request> request)
            throws Exception;

    @ApiOperation("绑定手机-前台")
    @PostMapping("/bindMobile")
    APIResponse<Integer> bindMobile(@RequestBody APIRequest<BindMobileRequest> request) throws Exception;

    @ApiOperation("绑定手机-前台")
    @PostMapping("/bindMobileV2")
    APIResponse<Integer> bindMobileV2(@RequestBody APIRequest<BindMobileV2Request> request) throws Exception;

    @ApiOperation("发送手机绑定验证码-前台")
    @PostMapping("/sendBindMobileVerifyCode")
    APIResponse<SendBindMobileVerifyCodeResponse> sendBindMobileVerifyCode(
            @RequestBody APIRequest<SendBindMobileVerifyCodeRequest> request) throws Exception;

    @ApiOperation("发送邮箱绑定验证码-前台")
    @PostMapping("/sendBindEmailVerifyCode")
    APIResponse<SendBindEmailVerifyCodeResponse> sendBindEmailVerifyCode(
            @RequestBody APIRequest<SendBindEmailVerifyCodeRequest> request) throws Exception;

    @ApiOperation("绑定邮箱-前台")
    @PostMapping("/bindEmail")
    APIResponse<BindEmailResponse> bindEmail(@RequestBody APIRequest<BindEmailRequest> request) throws Exception;



    @ApiOperation("发送邮箱验证码-前台")
    @PostMapping("/sendEmailVerifyCode")
    APIResponse<SendEmailVerifyCodeResponse> sendEmailVerifyCode(
            @RequestBody APIRequest<SendEmailVerifyCodeRequest> request) throws Exception;

    @ApiOperation("打开手机验证-前台")
    @PostMapping("/openMobileVerify")
    APIResponse<Integer> openMobileVerify(@RequestBody APIRequest<OpenOrCloseMobileVerifyRequest> request)
            throws Exception;

    @ApiOperation("关闭手机验证-前台")
    @PostMapping("/closeMobileVerify")
    APIResponse<Integer> closeMobileVerify(@RequestBody APIRequest<OpenOrCloseMobileVerifyRequest> request)
            throws Exception;

    @ApiOperation("打开BNB燃烧-前台")
    @PostMapping("/openBNBFee")
    APIResponse<Integer> openBNBFee(@RequestBody APIRequest<OpenOrCloseBNBFeeRequest> request) throws Exception;

    @ApiOperation("关闭BNB燃烧-前台")
    @PostMapping("/closeBNBFee")
    APIResponse<Integer> closeBNBFee(@RequestBody APIRequest<OpenOrCloseBNBFeeRequest> request) throws Exception;

    @ApiOperation("开启提币白名单-前台")
    @PostMapping("/openWithdrawWhiteStatus")
    APIResponse<Integer> openWithdrawWhiteStatus(@RequestBody APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request)
            throws Exception;

    @ApiOperation("开启提币白名单-前台")
    @PostMapping("/openWithdrawWhiteStatusV2")
    APIResponse<OpenWithdrawWhiteStatusV2Response> openWithdrawWhiteStatusV2(@RequestBody APIRequest<OpenWithdrawWhiteStatusV2Request> request)
            throws Exception;

    @ApiOperation("关闭提币白名单-前台")
    @PostMapping("/closeWithdrawWhiteStatus")
    APIResponse<CloseWithdrawWhiteStatusResponse> closeWithdrawWhiteStatus(
            @RequestBody APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request) throws Exception;

    @ApiOperation("关闭提币白名单-前台")
    @PostMapping("/closeWithdrawWhiteStatusV2")
    APIResponse<CloseWithdrawWhiteStatusV2Response> closeWithdrawWhiteStatusV2(
            @RequestBody APIRequest<CloseWithdrawWhiteStatusV2Request> request) throws Exception;

    @ApiOperation("确定关闭提币白名单-前台")
    @PostMapping("/confirmCloseWithdrawWhiteStatus")
    APIResponse<ConfirmCloseWithdrawWhiteStatusResponse> confirmCloseWithdrawWhiteStatus(
            @RequestBody APIRequest<ConfirmCloseWithdrawWhiteStatusRequest> request) throws Exception;

    @ApiOperation("设置防钓鱼码-前台")
    @PostMapping("/aouAntiPhishingCode")
    APIResponse<Integer> aouAntiPhishingCode(@RequestBody APIRequest<BindPhishingCodeRequest> request) throws Exception;

    @ApiOperation("设置防钓鱼码-前台")
    @PostMapping("/aouAntiPhishingCodeV2")
    APIResponse<Integer> aouAntiPhishingCodeV2(@RequestBody APIRequest<BindPhishingCodeV2Request> request) throws Exception;

    @ApiOperation("禁用用户-前台")
    @PostMapping("/forbidden-user")
    APIResponse<Integer> forbiddenUser(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("完整禁用用户-前台")
    @PostMapping("/forbidden-user-total")
    APIResponse<Integer> forbiddenUserTotal(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("点击链接禁用用户-前台")
    @PostMapping("/forbidden/code")
    APIResponse<Boolean> forbidByCode(@Validated @RequestBody APIRequest<UserForbidRequest> request);


    @ApiOperation("生成账户禁用码")
    @PostMapping("/forbidden/code/generate")
    APIResponse<String> generateForbidCode(@Validated @RequestBody APIRequest<UserIdRequest> request);

    @ApiOperation("查看code是否有效")
    @GetMapping("/forbidden/code/check")
    APIResponse<CheckForbidCodeResponse> checkForbidCode(@RequestParam("code") String code);

    @ApiOperation("锁定用户-前台")
    @PostMapping("/lock-user")
    APIResponse<?> lockUser(@Validated @RequestBody APIRequest<UserLockRequest> request) throws Exception;

    @ApiOperation("大户保护确定ip变更-前台")
    @PostMapping("/confirmedUserIpChange")
    APIResponse<ConfirmedUserIpChangeResponse> confirmedUserIpChange(
            @RequestBody APIRequest<ConfirmedUserIpChangeRequest> request) throws Exception;


    @ApiOperation("批量查询用户基本信息")
    @PostMapping("/selectUserSecurityList")
    APIResponse<UserSecurityListResponse> selectUserSecurityList(
            @Validated @RequestBody APIRequest<GetUserListRequest> request) throws Exception;

    /** --- 以下为后台管理系统接口 --- **/

    @ApiOperation("设置用户安全级别-后台")
    @PostMapping("/level/setting")
    APIResponse<Integer> setSecurityLevel(@Validated @RequestBody APIRequest<SecurityLevelSettingRequest> request)
            throws Exception;

    @ApiOperation("批量设置用户安全级别-后台")
    @PostMapping("/batchUpdateSecurityUserLevel")
    APIResponse<List<String>> batchUpdateSecurityUserLevel(
            @Validated @RequestBody APIRequest<BatchUpdateSecurityLevelRequest> request) throws Exception;

    @ApiOperation("重置用户二次验证-后台")
    @PostMapping("/second-validation/reset")
    APIResponse<Integer> resetSecurity(@Validated @RequestBody APIRequest<ResetSecurityRequest> request)
            throws Exception;

    @ApiOperation("一键启用用户-后台")
    @PostMapping("/oneButtonActivation")
    APIResponse<?> oneButtonActivation(@Validated @RequestBody APIRequest<OneButtonActivationRequest> request)
            throws Exception;

    @ApiOperation("一键启用禁用-后台")
    @PostMapping("/oneButtonDisable")
    APIResponse<?> oneButtonDisable(@Validated @RequestBody APIRequest<OneButtonDisableRequest> request)
            throws Exception;

    @ApiOperation("风控禁用交易以及撤单")
    @PostMapping("/disableTradeAndCancleOrder")
    APIResponse<?> disableTradeAndCancleOrder(@Validated @RequestBody APIRequest<OneButtonDisableRequest> request)
            throws Exception;

    @ApiOperation("解禁用户成功后的启用用户")
    @PostMapping("/resetActivationUser")
    APIResponse<?> resetActivationUser(@Validated @RequestBody APIRequest<ResetActivationUserRequest> request);

    @ApiOperation("ResetApply禁用用户提币功能")
    @PostMapping("/resetDisableTrading")
    APIResponse<?> resetDisableTrading(@Validated @RequestBody APIRequest<ResetDisableTradingRequest> request);

    @ApiOperation("禁用登录-后台")
    @PostMapping("/disableLogin")
    APIResponse<?> disableLogin(@Validated @RequestBody APIRequest<DisableLoginRequest> request)
            throws Exception;

    @ApiOperation("启用登录-后台")
    @PostMapping("/activeLogin")
    APIResponse<?> activeLogin(@Validated @RequestBody APIRequest<ActiveLoginRequest> request)
            throws Exception;

    @ApiOperation("重置谷歌验证-后台")
    @PostMapping("/resetGoogleVerify")
    APIResponse<UnbindGoogleVerifyResponse> resetGoogleVerify(@RequestBody APIRequest<ResetGoogleRequest> request)
            throws Exception;


    /** --- 以下暂未使用 --- **/

    @ApiOperation("重置登录错误次数")
    @PostMapping("/resetLoginFailedNum")
    APIResponse<Integer> resetLoginFailedNum(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("根据用户Id更新用户安全信息")
    @PostMapping("/updateUserSecurityByUserId")
    APIResponse<Integer> updateUserSecurityByUserId(@RequestBody APIRequest<UpdateUserSecurityByUserIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户Id获取用户安全信息")
    @PostMapping("/getUserSecurityByUserId")
    APIResponse<UserSecurityVo> getUserSecurityByUserId(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户邮箱获取用户安全信息")
    @PostMapping("/getUserSecurityByEmail")
    APIResponse<UserSecurityVo> getUserSecurityByEmail(@RequestBody APIRequest<UserEmailRequest> request)
            throws Exception;

    @ApiOperation("判断手机号是否存在")
    @PostMapping("/isMobileExist")
    APIResponse<Boolean> isMobileExist(@Validated @RequestBody APIRequest<MobileRequest> request) throws Exception;

    @ApiOperation("判断手机号是否存在(限流版本)")
    @PostMapping("/isMobileExistRateLimit")
    APIResponse<Boolean> isMobileExistRateLimit(@Validated @RequestBody APIRequest<MobileRateLimitRequest> request) throws Exception;

    @ApiOperation("查防钓鱼码")
    @PostMapping("/selectAntiPhishingCode")
    APIResponse<String> selectAntiPhishingCode(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("验证手机验证码/谷歌验证码")
    @PostMapping("/verificationsTwo")
    APIResponse<String> verificationsTwo(@Validated @RequestBody APIRequest<VarificationTwoRequest> request)
            throws Exception;

    @ApiOperation("验证Yubikey/手机验证码/谷歌验证码")
    @PostMapping("/verificationsTwoV2")
    APIResponse<String> verificationsTwoV2(@Validated @RequestBody APIRequest<VerificationTwoV2Request> request)
            throws Exception;


    @ApiOperation("验证邮箱验证码/手机验证码/谷歌验证码")
    @PostMapping("/verificationsTwoV3")
    APIResponse<VerificationTwoV3Response> verificationsTwoV3(@Validated @RequestBody APIRequest<VerificationTwoV3Request> request)
            throws Exception;


    @ApiOperation("按需验证邮箱验证码/手机验证码/谷歌验证码")
    @PostMapping("/verificationsDemand")
    APIResponse<VerificationsDemandResponse> verificationsDemand(@Validated @RequestBody APIRequest<VerificationsDemandRequest> request)
            throws Exception;


    @ApiOperation("获取需要检验的2fa列表（验证邮箱验证码/手机验证码/谷歌验证码）其中一个或者多个")
    @PostMapping("/getVerificationTwoCheckList")
    APIResponse<GetVerificationTwoCheckListResponse> getVerificationTwoCheckList(@Validated @RequestBody APIRequest<GetVerificationTwoCheckListRequest> request)
            throws Exception;

    @ApiOperation("修改禁用状态")
    @PostMapping("/updateStatusByUserId")
    @Deprecated
    APIResponse<Integer> updateStatusByUserId(@Validated @RequestBody APIRequest<SecurityStatusRequest> request)
            throws Exception;

    @ApiOperation("新版修改禁用状态")
    @PostMapping("/updateWithdrawStatusByUserId")
    APIResponse<Integer> updateWithdrawStatusByUserId(@Validated @RequestBody APIRequest<UpdateWithdrawStatusRequest> request)
            throws Exception;

    @ApiOperation("获取用户密码修改时间")
    @PostMapping("/password/update-time")
    APIResponse<Long> getPasswordUpdateTime(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("获取用户密码、用户邮箱修改时间")
    @PostMapping("/account/update-time")
    public APIResponse<AccountUpdateTimeForTrade> getAccountUpdateTimeForTrade(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("生成gAuth密钥")
    @PostMapping("/generator/gauth/key")
    APIResponse<GoogleAuthKeyResp> generateAuthKeyAndQrCode(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("获取用户解绑2FA时间")
    @PostMapping("/2fa/unbind-time")
    APIResponse<Long> get2FaUnbindTime(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("修改提现风控是否需要做人脸识别标识")
    @PostMapping("/withdrawSecurityFaceStatus")
    APIResponse<Integer> changeWithdrawSecurityFaceStatus(@Validated @RequestBody APIRequest<SecurityFaceStatusRequest> request);

    @ApiOperation("直接变更用户提币风控人脸识别标识")
    @PostMapping("/withdrawFaceStatusChange")
    APIResponse<Integer> changeWithdrawFaceCheckStatus(@Validated @RequestBody APIRequest<WithdrawFaceStatusChangeRequest> request);

    @ApiOperation("是否需要重新绑定谷歌验证")
    @PostMapping("/reBindGoogleVerifyStatus")
    APIResponse<Boolean> getReBindGoogleVerifyStatus(@Validated @RequestBody APIRequest<GetReBindGoogleVerifyStatusRequest> request);

    @ApiOperation("当前应用场景是否启用Security key")
    @PostMapping("/security-key/scenario/enabled")
    APIResponse<Boolean> isSecurityKeyEnabledInSpecifiedScenario(@Validated @RequestBody APIRequest<IsSecurityKeyEnabledRequest> request);

    @ApiOperation("更新用户的Security key使用场景")
    @PostMapping("/security-key/scenario/update")
    APIResponse<Void> updateSecurityKeyApplicationScenarios(@Validated @RequestBody APIRequest<UpdateSecurityKeyApplicationScenarioRequest> request);

    @ApiOperation("禁用交易，提币-fiat")
    @PostMapping("/fiat/disable")
    APIResponse<?> disableForFiat(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("获取禁止提现时间")
    @PostMapping("/account/getWithdrawTimeForTrade")
    public APIResponse<WithdrawTimeForTradeResponse> getWithdrawTimeForTrade(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;


    @ApiOperation("开启快速提币开关")
    @PostMapping("/enableFastWithdrawSwitch")
    APIResponse<EnableFastWithdrawSwitchResponse> enableFastWithdrawSwitch(@RequestBody APIRequest<EnableFastWithdrawSwitchRequest> request) throws Exception;

    @ApiOperation("关闭快速提币开关")
    @PostMapping("/disableFastWithdrawSwitch")
    APIResponse<DisableFastWithdrawSwitchResponse> disableFastWithdrawSwitch(@RequestBody APIRequest<DisableFastWithdrawSwitchRequest> request) throws Exception;





    @ApiOperation("开启资金密码")
    @PostMapping("/enableFundPassword")
    APIResponse<EnableFundPasswordResponse> enableFundPassword(@RequestBody APIRequest<EnableFundPasswordRequest> request) throws Exception;

    @ApiOperation("关闭资金密码")
    @PostMapping("/disableFundPassword")
    APIResponse<DisableFundPasswordResponse> disableFundPassword(@RequestBody APIRequest<DisableFundPasswordRequest> request) throws Exception;


    @ApiOperation("重置资金密码")
    @PostMapping("/resetFundPassword")
    APIResponse<ResetFundPasswordResponse> resetFundPassword(@RequestBody APIRequest<ResetFundPasswordRequest> request) throws Exception;


    @ApiOperation("验证资金密码")
    @PostMapping("/verifyFundPassword")
    APIResponse<VerifyFundPasswordResponse> verifyFundPassword(@RequestBody APIRequest<VerifyFundPasswordRequest> request) throws Exception;
    @ApiOperation("通过userid获取用户的真实邮箱或者手机号")
    @PostMapping("/getUserEmailAndMobileByUserId")
    APIResponse<GetUserEmailAndMobileByUserIdResponse> getUserEmailAndMobileByUserId(@RequestBody APIRequest<GetUserEmailAndMobileByUserIdRequest> request) throws Exception;


    @ApiOperation("通过手机号或者真实邮箱获取用户的userid")
    @PostMapping("/getUserIdByMobileOrEmail")
    APIResponse<GetUserIdByEmailOrMobileResponse> getUserIdByMobileOrEmail(@RequestBody APIRequest<GetUserIdByEmailOrMobileRequest> request) throws Exception;



    @ApiOperation("忘记密码precheck")
    @PostMapping("/forgotPasswordPreCheck")
    APIResponse<AccountForgotPasswordPreCheckResponse> forgotPasswordPreCheck(
            @RequestBody APIRequest<AccountForgotPasswordPreCheckRequest> request) throws Exception;

    @ApiOperation("重置密码V2")
    @PostMapping("/resetPasswordV2")
    APIResponse<AccountResetPasswordResponseV2> resetPasswordV2(
            @RequestBody APIRequest<AccountResetPasswordRequestV2> request) throws Exception;


    @ApiOperation("获取用户的2fa绑定信息")
    @PostMapping("/2fa/query")
    APIResponse<GetUser2faResponse> getUser2fa(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;
    @ApiOperation("获取2fa列表")
    @PostMapping("/get2FaVerifyList")
    APIResponse<MultiFactorSceneCheckResponse> get2FaVerifyList(@RequestBody APIRequest<MultiFactorSceneCheckRequest> request);


    @ApiOperation("用户安全风控监控信息")
    @GetMapping("/getUserRiskInfo")
    APIResponse<UserRiskInfoResponse> getUserRiskInfo(@RequestParam("userId") Long userId);



    @ApiOperation("换绑邮箱")
    @PostMapping("/changeEmail")
    APIResponse<ChangeEmailResponse> changeEmail(@RequestBody APIRequest<ChangeEmailRequest> request)
            throws Exception;


    @ApiOperation("换绑手机")
    @PostMapping("/changeMobile")
    APIResponse<ChangeMobileResponse> changeMobile(@RequestBody APIRequest<ChangeMobileRequest> request)
            throws Exception;

    @ApiOperation("获取PNK库里面的提现记录信息")
    @GetMapping("/getUserWithdrawDailyLimitModify")
    APIResponse<OldWithdrawDailyLimitModifyVo> getOldWithdrawDailyLimitModify(@RequestParam("userId") Long userId);

    @ApiOperation("批量获取PNK库提币记录备注信息")
    @PostMapping("/getOldWithdrawDailyLimitModifyCause")
    APIResponse<Map<Long, String>> getOldWithdrawDailyLimitModifyCause(@Validated @RequestBody APIRequest<GetUserListRequest> request);


    @ApiOperation("getCapitalWithdrawVerfiyParam")
    @PostMapping("/getCapitalWithdrawVerfiyParam")
    APIResponse<GetCapitalWithdrawVerifyParamResponse> getCapitalWithdrawVerfiyParam(@RequestBody APIRequest<GetCapitalWithdrawVerifyParamRequest> request)
            throws Exception;


}
