package com.binance.account.api;

import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.request.BatchUpdateSecurityLevelRequest;
import com.binance.account.vo.security.request.BindGoogleVerifyRequest;
import com.binance.account.vo.security.request.BindMobileRequest;
import com.binance.account.vo.security.request.BindPhishingCodeRequest;
import com.binance.account.vo.security.request.ConfirmCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.ConfirmedUserIpChangeRequest;
import com.binance.account.vo.security.request.GetReBindGoogleVerifyStatusRequest;
import com.binance.account.vo.security.request.IsSecurityKeyEnabledRequest;
import com.binance.account.vo.security.request.MobileRequest;
import com.binance.account.vo.security.request.OneButtonActivationRequest;
import com.binance.account.vo.security.request.OneButtonDisableRequest;
import com.binance.account.vo.security.request.OpenOrCloseBNBFeeRequest;
import com.binance.account.vo.security.request.OpenOrCloseMobileVerifyRequest;
import com.binance.account.vo.security.request.OpenOrCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.ResetGoogleRequest;
import com.binance.account.vo.security.request.ResetSecurityRequest;
import com.binance.account.vo.security.request.SecurityFaceStatusRequest;
import com.binance.account.vo.security.request.SecurityLevelSettingRequest;
import com.binance.account.vo.security.request.SecurityStatusRequest;
import com.binance.account.vo.security.request.SendBindMobileVerifyCodeRequest;
import com.binance.account.vo.security.request.UnbindGoogleRequest;
import com.binance.account.vo.security.request.UnbindMobileRequest;
import com.binance.account.vo.security.request.UpdateSecurityKeyApplicationScenarioRequest;
import com.binance.account.vo.security.request.UpdateUserSecurityByUserIdRequest;
import com.binance.account.vo.security.request.UserEmailRequest;
import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.security.request.VarificationTwoRequest;
import com.binance.account.vo.security.request.VerificationTwoV2Request;
import com.binance.account.vo.security.request.*;
import com.binance.account.vo.security.response.*;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户安全")
@RequestMapping("/userSecurity")
public interface UserSecurityApi {


    /** --- 以下为前台用户接口 --- **/

    @ApiOperation("解绑谷歌验证-前台")
    @PostMapping("/unbindGoogleVerify")
    APIResponse<UnbindGoogleVerifyResponse> unbindGoogleVerify(@RequestBody APIRequest<UnbindGoogleRequest> request)
            throws Exception;

    @ApiOperation("绑定谷歌验证-前台")
    @PostMapping("/bindGoogleVerify")
    APIResponse<BindGoogleVerifyResponse> bindGoogleVerify(@RequestBody APIRequest<BindGoogleVerifyRequest> request)
            throws Exception;

    @ApiOperation("解绑手机-前台")
    @PostMapping("/unbindMobile")
    APIResponse<UnbindMobileResponse> unbindMobile(@RequestBody APIRequest<UnbindMobileRequest> request)
            throws Exception;

    @ApiOperation("绑定手机-前台")
    @PostMapping("/bindMobile")
    APIResponse<Integer> bindMobile(@RequestBody APIRequest<BindMobileRequest> request) throws Exception;

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

    @ApiOperation("解绑邮箱-前台")
    @PostMapping("/unbindEmail")
    APIResponse<UnbindEmailResponse> unbindEmail(@RequestBody APIRequest<UnbindEmailRequest> request)
            throws Exception;

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

    @ApiOperation("禁用用户-前台")
    @PostMapping("/forbidden-user")
    APIResponse<Integer> forbiddenUser(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

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

    @ApiOperation("获取用户的2fa绑定信息")
    @PostMapping("/2fa/query")
    APIResponse<GetUser2faResponse> getUser2fa(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

}
