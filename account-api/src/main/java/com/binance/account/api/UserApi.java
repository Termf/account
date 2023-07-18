package com.binance.account.api;

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
import com.binance.master.commons.SearchResult;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/user")
@Api(value = "用户")
public interface UserApi {

    @ApiOperation(notes = "注册", nickname = "register", value = "注册")
    @PostMapping("/register")
    APIResponse<RegisterUserResponse> register(@RequestBody() APIRequest<RegisterUserRequest> request) throws Exception;

    @ApiOperation(notes = "注册V2", nickname = "register", value = "注册")
    @PostMapping("/registerV2")
    APIResponse<RegisterUserResponseV2> registerV2(@RequestBody() APIRequest<RegisterUserRequestV2> request) throws Exception;

    @ApiOperation(notes = "登录", nickname = "login", value = "登录")
    @PostMapping("/login")
    APIResponse<LoginUserResponse> login(@RequestBody() APIRequest<LoginUserRequest> request,
            @ApiIgnore() @RequestParam("bindingResult") BindingResult bindingResult) throws Exception;

    @ApiOperation(notes = "登录V2", nickname = "login", value = "登录")
    @PostMapping("/loginV2")
    APIResponse<LoginUserResponseV2> loginV2(@RequestBody() APIRequest<LoginUserRequestV2> request,
                                             @ApiIgnore() @RequestParam(name = "bindingResult", required = false) BindingResult bindingResult) throws Exception;

    /*
     * @ApiOperation(notes = "登录失败次数更新", nickname = "loginNumUpdate", value = "登录失败次数更新")
     *
     * @PostMapping("/loginNumUpdate") APIResponse<Integer> loginNumUpdate(@RequestBody()
     * APIRequest<UserIdRequest> request) throws Exception;
     */

    @ApiOperation(notes = "修改用户密码", nickname = "updatePwd", value = "修改用户密码")
    @PostMapping("/updatePwd")
    APIResponse<UpdatePwdUserResponse> updatePwd(@RequestBody() APIRequest<UpdatePwdUserRequest> request)
            throws Exception;

    @ApiOperation(notes = "修改用户账号(尽量少改)", nickname = "updateAccount", value = "修改用户账号")
    @PostMapping("/updateAccount")
    APIResponse<UpdateAccountUserResponse> updateAccount(@RequestBody() APIRequest<UpdateAccountUserRequest> request)
            throws Exception;

    @ApiOperation(notes = "账号激活", nickname = "accountActive", value = "账号激活")
    @PostMapping("/accountActive")
    APIResponse<AccountActiveUserResponse> accountActive(@RequestBody() APIRequest<AccountActiveUserRequest> request)
            throws Exception;

    @ApiOperation(notes = "账号激活", nickname = "accountActiveV2", value = "账号激活")
    @PostMapping("/accountActiveV2")
    APIResponse<AccountActiveUserV2Response> accountActiveV2(@RequestBody() APIRequest<AccountActiveUserV2Request> request)
            throws Exception;

    @ApiOperation("根据Email获取用户信息")
    @PostMapping("/getUserByEmail")
    APIResponse<GetUserResponse> getUserByEmail(@RequestBody APIRequest<GetUserRequest> request) throws Exception;

    @ApiOperation("根据手机号获取用户信息")
    @PostMapping("/getUserByMobile")
    APIResponse<GetUserResponse> getUserByMobile(@RequestBody APIRequest<UserMobileRequest> request) throws Exception;

    @ApiOperation("根据UserID获取用户信息")
    @PostMapping("/getUserById")
    APIResponse<GetUserResponse> getUserById(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("根据UserID获取用户信息(从主库读)")
    @PostMapping("/getUserByIdFromMasterDb")
    APIResponse<GetUserResponse> getUserByIdFromMasterDb(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("根据email更新用户")
    @PostMapping("/updateUserByEmail")
    APIResponse<Integer> updateUserByEmail(@RequestBody APIRequest<UpdateUserByEmailRequest> request) throws Exception;

    @ApiOperation("忘记密码")
    @PostMapping("/forgotPasswordSendEmail")
    APIResponse<AccountForgotPasswordResponse> forgotPasswordSendEmail(
            @RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception;

    @ApiOperation("重置密码")
    @PostMapping("/resetPassword")
    APIResponse<AccountResetPasswordResponse> resetPassword(
            @RequestBody APIRequest<AccountResetPasswordRequest> request) throws Exception;

    @ApiOperation("启用用户状态")
    @PostMapping("/enableUserStatus")
    APIResponse<Integer> enableUserStatus(@RequestBody APIRequest<UserStatusRequest> request) throws Exception;

    @ApiOperation("停用用户状态")
    @PostMapping("/disableUserStatus")
    APIResponse<Integer> disableUserStatus(@RequestBody APIRequest<UserStatusRequest> request) throws Exception;

    @ApiOperation("重新发送激活码")
    @PostMapping("/resendSendActiveCode")
    APIResponse<ResendSendActiveCodeResponse> resendSendActiveCode(
            @RequestBody APIRequest<ResendSendActiveCodeRequest> request) throws Exception;

    @ApiOperation("发送用户手机认证码")
    @PostMapping("/sendSmsAuthCode")
    APIResponse<SendSmsAuthCoderResponse> sendSmsAuthCode(@RequestBody APIRequest<SendSmsAuthCoderRequest> request)
            throws Exception;

    @ApiOperation("发送用户手机认证码")
    @PostMapping("/sendSmsAuthCodeV2")
    APIResponse<SendSmsAuthCodeV2Response> sendSmsAuthCodeV2(@RequestBody APIRequest<SendSmsAuthCodeV2Request> request)
            throws Exception;

    @ApiOperation("根据用户Id查询用户邮箱")
    @PostMapping("/getUserEmailByUserId")
    APIResponse<GetUserEmailResponse> getUserEmailByUserId(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户Id批量查询用户邮箱")
    @PostMapping("/getUserEmailByUserIds")
    APIResponse<GetUserEmailsResponse> getUserEmailByUserIds(@RequestBody APIRequest<GetUserListRequest> request)
            throws Exception;

    @ApiOperation("根据用户email批量查询用户 id")
    @PostMapping("/getUserIdByEmails")
    APIResponse<GetUserEmailsResponse> getUserIdByEmails(@RequestBody APIRequest<GetUserIdListRequest> request)
            throws Exception;

    @ApiOperation("密码检查")
    @PostMapping("/passwordCheck")
    APIResponse<Boolean> passwordCheck(@RequestBody APIRequest<PasswordVerifyRequest> request) throws Exception;

    @ApiOperation("搜索用户列表")
    @PostMapping("/searchUserList")
    APIResponse<SearchUserListResponse> searchUserList(@RequestBody APIRequest<SearchUserListRequest> request)
            throws Exception;

    @ApiOperation("用户基础信息")
    @PostMapping("/baseDetail")
    APIResponse<BaseDetailResponse> baseDetail(@RequestBody APIRequest<BaseDetailRequest> request) throws Exception;


    @ApiOperation("用户摘要信息")
    @PostMapping("/brief-info")
    APIResponse<UserBriefInfoResponse> briefInfo(@RequestBody APIRequest<UserIdReq> request) throws Exception;


    @ApiOperation("根据email获取userId")
    @PostMapping("/getUserIdByEmail")
    APIResponse<Long> getUserIdByEmail(@RequestBody APIRequest<GetUserRequest> request) throws Exception;

    @ApiOperation("批量获取用户信息")
    @PostMapping("/getUserListByUserIds")
    APIResponse<GetUserListResponse> getUserListByUserIds(@RequestBody APIRequest<GetUserListRequest> request)
            throws Exception;

    @ApiOperation("根据tradingAccount获取userId")
    @PostMapping("/getUserIdByTradingAccount")
    APIResponse<Long> getUserIdByTradingAccount(@RequestBody APIRequest<GetUserIdByTradingAccountRequest> request)
            throws Exception;

    @ApiOperation("APP忘记密码-发送验证码邮件")
    @PostMapping("/app/password/forgot")
    APIResponse<AccountForgotPasswordResponse> sendAppForgotPasswordEmail(
            @RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception;

    @ApiOperation("APP重置密码验证码校验")
    @PostMapping("/app/password/verify")
    APIResponse<AccountResetPasswordVerifyResponse> resetAppPasswordVerify(
            @RequestBody APIRequest<AccountResetPasswordVerifyRequest> request) throws Exception;

    @ApiOperation("APP重置密码")
    @PostMapping("/app/password/reset")
    APIResponse<AccountResetPasswordResponse> resetAppPassword(
            @RequestBody APIRequest<AccountResetPasswordRequest> request) throws Exception;

    @ApiOperation("修改用户")
    @PostMapping("/modification")
    APIResponse<Integer> modifyUser(@Validated @RequestBody APIRequest<ModifyUserRequest> request) throws Exception;

    @ApiOperation("修改用户昵称")
    @PostMapping("/updateNickName")
    APIResponse<Integer> updateNickName(@Validated @RequestBody APIRequest<UpdateNickNameRequest> request)
            throws Exception;

    @ApiOperation("修改用户邮箱")
    @PostMapping("/modifyUserEmail")
    APIResponse<Integer> modifyUserEmail(@Validated @RequestBody APIRequest<ModifyUserEmailRequest> request) throws Exception;

    @ApiOperation("删除用户")
    @PostMapping("/deleteUser")
    APIResponse<Integer> deleteUser(@Validated @RequestBody APIRequest<DeleteUserRequest> request) throws Exception;

    @ApiOperation(value = "检查用户是否存在", nickname = "方便测试使用")
    @PostMapping("/isExist")
    public APIResponse<Boolean> isExist(APIRequest<String> request) throws Exception;

    @ApiOperation(value = "根据入参查userIp个数")
    @PostMapping("/ipIsExist")
    public APIResponse<Boolean> ipIsExist(@Validated @RequestBody APIRequest<UserIpRequest> request) throws Exception;

    @ApiOperation("用户kyc,地址认证信息")
    @PostMapping("/kycValidateInfo")
    APIResponse<List<KycValidateResponse>> kycValidateInfo(@RequestBody APIRequest<BaseDetailRequest> request)
            throws Exception;

    @ApiOperation(value = "统计当天注册用户量")
    @PostMapping("/todayRegist")
    public APIResponse<Integer> todayRegist(@Validated @RequestBody APIRequest<UserRegistRequest> request)
            throws Exception;

    @ApiOperation(value = "记录用户ip")
    @PostMapping("/saveUserIp")
    public APIResponse<Integer> saveUserIp(@Validated @RequestBody APIRequest<UserIpRequest> request) throws Exception;

    @ApiOperation(notes = "账号激活(仅供内部使用)", nickname = "internalAccountActive", value = "账号激活(仅供内部使用)")
    @PostMapping("/internal/account/active")
    APIResponse<AccountActiveUserResponse> internalAccountActive(@RequestBody() APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("检查IP是否匹配")
    @PostMapping("/ipLikeCheck")
    public APIResponse<UserIpLikeVo> ipLikeCheck(@RequestBody() APIRequest<UserIpLikeVo> request) throws Exception;

    @ApiOperation("获取用户的所有IP信息")
    @PostMapping("/getUserIpList")
    APIResponse<List<UserIpVo>> getUserIpList(@Validated @RequestBody APIRequest<UserIdRequest> request);


    @ApiOperation(notes = "处理越南用户数据", nickname = "fixVNUser", value = "处理越南用户数据")
    @PostMapping("/internal/account/fixVNUser")
    public APIResponse<Boolean> fixVNUser(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("根据UserId获取用户状态")
    @PostMapping("/status")
    APIResponse<UserStatusEx> getUserStatusByUserId(@RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("根据UserIds批量获取用户状态")
    @PostMapping("/statusByUserIds")
    APIResponse<List<UserVo>> getUserStatusByUserIds(@RequestBody APIRequest<GetUserListRequest> request) throws Exception;


    @ApiOperation("保存recaptcha响应结果")
    @PostMapping("/save/recaptcha")
    APIResponse<Integer> saveRecaptcha(@RequestBody APIRequest<ReCaptchaReq> request) throws Exception;

    @ApiOperation("统计某个推荐人的推荐用户数量")
    @PostMapping("/countAgentNumber")
    APIResponse<Long> countAgentNumber(@RequestBody APIRequest<CountAgentNumberRequest> request) throws Exception;


    @ApiOperation("根据userid创建margin账户")
    @PostMapping("/createMarginAccount")
    APIResponse<CreateMarginUserResponse> createMarginAccount(@RequestBody @Validated APIRequest<CreateMarginAccountRequest> request)
            throws Exception;

    @ApiOperation("根据userid创建fiat账户")
    @PostMapping("/createFiatAccount")
    APIResponse<CreateFiatUserResponse> createFiatAccount(@RequestBody @Validated APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("判断两个userid是否是margin的关系")
    @PostMapping("/isMarginRelationShip")
    APIResponse<Boolean> isMarginRelationShip(@RequestBody APIRequest<MarginRelationShipRequest> request) throws Exception;


    @ApiOperation("通过useridlist获取账号类型信息")
    @PostMapping("/getBatchUserTypeList")
    APIResponse<List<GetBatchUserTypeListResponse>> getBatchUserTypeList(@RequestBody APIRequest<GetBatchUserTypeListRequest> request) throws Exception;


    @ApiOperation("根据主账户userid来获取相应类型的accountid")
    @PostMapping("/getAccountIdByRootUserId")
    APIResponse<Long> getAccountIdByRootUserId(@RequestBody APIRequest<GetAccountIdByRootUserIdRequest> request) throws Exception;

    @ApiOperation("获取推荐人详情")
    @PostMapping("/getUserAgentDetail")
    APIResponse<List<GetUserResponse>> getUserAgentDetail(@RequestBody APIRequest<GetUserAgentDetailRequest> request) throws Exception;


    @ApiOperation("模糊匹配用户index信息")
    @PostMapping("/fuzzyMatchUserIndex")
    APIResponse<FuzzyMatchUserIndexResponse> fuzzyMatchUserIndex(@RequestBody APIRequest<FuzzyMatchUserIndexRequest> request) throws Exception;

    @ApiOperation("模糊匹配用户info信息")
    @PostMapping("/fuzzyMatchUserInfo")
    APIResponse<FuzzyMatchUserInfoResponse> fuzzyMatchUserInfo(@RequestBody APIRequest<FuzzyMatchUserInfoRequest> request) throws Exception;


    @ApiOperation("用户返佣详情信息")
    @PostMapping("/getUserCommissionDetail")
    APIResponse<GetUserCommissionDetailResponse> getUserCommissionDetail(@RequestBody APIRequest<GetUserAgentDetailRequest> request) throws Exception;

    @ApiOperation("获取特殊用户ID")
    @PostMapping("/getSpecialUserIds")
    APIResponse<SpecialUserIdResponse> getSpecialUserIds() throws Exception;


    @ApiOperation("根据用户ID返回账号类型(普通|母|子|期货|借贷)")
    @PostMapping("/getUserTypeByUserId")
    APIResponse<UserTypeResponse> getUserTypeByUserId(@RequestBody() APIRequest<UserIdReq> request)
            throws Exception;

    @ApiOperation("根据testnet的email，判断用户在主站是否存在")
    @PostMapping("/checkTestnetEmailIfPassKyc")
    APIResponse<Long> checkTestnetEmailIfPassKyc(@RequestBody() APIRequest<GetUserRequest> request)
            throws Exception;

    @ApiOperation("更新用户下单确认状态")
    @PostMapping("/updateOrderConfirmStatus")
    APIResponse<Integer> updateOrderConfirmStatus(@RequestBody APIRequest<OrderConfrimStatusRequest> request)
            throws Exception;

    @ApiOperation("生成推荐人返佣比率")
    @PostMapping("/createUserAgentRate")
    APIResponse<String> createUserAgentRate(@RequestBody APIRequest<UserAgentRateReq> request) throws Exception;

    @ApiOperation("根据id查询UserAgentRate")
    @PostMapping("/getUserAgentRateByCode")
    APIResponse<UserAgentRateResponse> getUserAgentRateByCode(@RequestBody APIRequest<String> request) throws Exception;

    @ApiOperation("根据UserId查询老式推荐UserPromote")
    @PostMapping("/getOldPromoteByUserId")
    APIResponse<GetUserAgentStatResponse> getOldPromoteByUserId(APIRequest<Long> request) throws Exception;

    @ApiOperation("根据UserId查询UserPromote")
    @PostMapping("/getUserPromoteByUserId")
    APIResponse<SearchResult<GetUserAgentStatResponse>> getUserPromoteByUserId(
            @RequestBody APIRequest<UserAgentLinkReq> request) throws Exception;

    @ApiOperation("根据推荐者userId查询被推荐用户邮箱")
    @PostMapping("/getReferralUserEmail")
    APIResponse<SearchResult<String>> getReferralUserEmail(@RequestBody APIRequest<GetReferralEmailRequest> request)
            throws Exception;

    @ApiOperation("根据agentCode更新label")
    @PostMapping("/updateLabelByAgentCode")
    APIResponse updateLabelByAgentCode(@RequestBody APIRequest<UpdateAgentRateReq> request) throws Exception;

    @ApiOperation("获取剩余的链接个数")
    @PostMapping("/getRemainingAgentLinkNum")
    APIResponse<Integer> getRemainingAgentLinkNum(@RequestBody APIRequest<Long> request) throws Exception;

    @ApiOperation("保存更新userAgentConfig")
    @PostMapping("/saveOrupdateUserAgentConfig")
    APIResponse<Boolean> saveOrupdateUserAgentConfig(@Validated @RequestBody APIRequest<UserAgentConfigReq> request)
            throws Exception;

    @ApiOperation("查询userAgentConfig")
    @PostMapping("/selectUserAgentConfig")
    APIResponse<SearchResult<GetUserAgentConfigResponse>> selectUserAgentConfig(
            @Validated @RequestBody APIRequest<GetUserAgentConfigRequest> request) throws Exception;

    @ApiOperation("查询getUserStatusByAgentCode")
    @PostMapping("/getUserStatusByAgentCode")
    public APIResponse<UserStatusEx> getUserStatusByAgentCode(@Validated @RequestBody APIRequest<String> request)
            throws Exception;

    @ApiOperation("选中agentCode作为分享")
    @PostMapping("/selectOneAsShareCode")
    public APIResponse<Boolean> selectOneAsShareCode(@Validated @RequestBody APIRequest<UserAgentSelectShareReq> request)
            throws Exception;

    @ApiOperation("保存更新截图配置信息")
    @PostMapping("/saveOrupdateSnapshotShareConfig")
    public APIResponse<Boolean> saveOrupdateSnapshotShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception;

    @ApiOperation("删除截图配置信息")
    @PostMapping("/deleteSnapshotShareConfig")
    public APIResponse<Boolean> deleteSnapshotShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception;

    @ApiOperation("查询截图配置信息")
    @PostMapping("/selectAllSnapShareConfig")
    public APIResponse<SnapshotShareConfigsRes> selectAllSnapShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception;

    @ApiOperation("获取选中的agentcode")
    @PostMapping("/selectCheckedShareCodeByUserId")
    public APIResponse<GetUserAgentStatResponse> selectCheckedShareCodeByUserId(@Validated @RequestBody APIRequest<Long> request)
            throws Exception;

    @ApiOperation("查询截图配置信息")
    @PostMapping("/selectAllSnapShareConfigForPnk")
    public APIResponse<List<SnapshotShareConfigRes>> selectAllSnapShareConfigForPnk(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request) throws Exception;

    @ApiOperation("统计某个推荐人的真实推荐人数量")
    @PostMapping("/countRealUserAgentNumber")
    public APIResponse<Long> countRealUserAgentNumber(@RequestBody APIRequest<CountAgentNumberRequest> request) throws Exception;

    @ApiOperation("签署LVT风险协议")
    @PostMapping("/signLVTRiskAgreement")
    public APIResponse<Boolean> signLVTRiskAgreement(@RequestBody() APIRequest<UserIdReq> request) throws Exception;

    @ApiOperation("取消签署LVT风险协议")
    @PostMapping("/cancelSignLVT")
    public APIResponse<Boolean> cancelSignLVT(@RequestBody() APIRequest<UserIdReq> request) throws Exception;

    @ApiOperation(notes = "第三方机构用户在binance注册", nickname = "thirdPartyUserRegister", value = "第三方机构用户在binance注册")
    @PostMapping("/thirdPartyUserRegister")
    APIResponse<RegisterUserResponse> thirdPartyUserRegister(@RequestBody() APIRequest<ThirdPartyUserRegisterRequest> request) throws Exception;

    @ApiOperation("第三方机构用户发送重置密码邮件")
    @PostMapping("/thirdPartySendResetPasswordEmail")
    APIResponse<AccountForgotPasswordResponse> thirdPartySendResetPasswordEmail(@RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception;

    @ApiOperation("用户签署LVT风险协议状态")
    @PostMapping("/signLVTStatus")
    APIResponse<SignLVTStatusResponse> signLVTStatus(@RequestBody APIRequest<UserIdReq> request) throws Exception;

    @ApiOperation(notes = "用户一键注册", nickname = "oneButtonRegister", value = "用户一键注册")
    @PostMapping("/oneButtonRegister")
    APIResponse<OneButtonRegisterResponse> oneButtonRegister(@RequestBody() APIRequest<OneButtonRegisterRequest> request) throws Exception;

    @ApiOperation("一键注册用户激活，并发送重置密码")
    @PostMapping("/oneButtonUserAccountActive")
    APIResponse<AccountActiveUserV2Response> oneButtonUserAccountActive(@RequestBody APIRequest<OneButtonUserAccountActiveRequest> request) throws Exception;

    @ApiOperation("查询矿池返佣")
    @PostMapping("/selectMiningUserAgentLog")
    APIResponse<SearchResult<SelectUserAgentLogResponse>> selectMiningUserAgentLog(APIRequest<SelectMiningAgentLogRequest> request) throws Exception;

    @ApiOperation("查询矿池返佣数量")
    @PostMapping("/selectMiningUserAgentNum")
    APIResponse<Long> selectMiningUserAgentNum(APIRequest<SelectUserAgentLogRequest> request) throws Exception;

    @ApiOperation("创建矿池返佣")
    @PostMapping("/createMiningAgentRate")
    APIResponse<String> createMiningAgentRate(@RequestBody @Validated APIRequest<Long> request) throws Exception;
}
