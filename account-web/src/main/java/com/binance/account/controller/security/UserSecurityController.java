package com.binance.account.controller.security;

import com.binance.account.aop.MarginValidate;
import com.binance.account.aop.SecurityLog;
import com.binance.account.api.UserSecurityApi;
import com.binance.account.constants.AccountConstants;
import com.binance.account.service.kyc.CertificateCenterDispatcher;
import com.binance.account.service.security.IUserForbid;
import com.binance.account.service.security.IUserIpChange;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.impl.UserSecurityResetHelper;
import com.binance.account.service.security.model.MultiFactorSceneCheckQuery;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.service.security.service.MultiFactorVerifyService;
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
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetUser2faResponse;
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
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.models.withdraw.OldWithdrawDailyLimitModify;
import com.binance.master.utils.RedisCacheUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class UserSecurityController implements UserSecurityApi {

    @Resource
    private IUserSecurity iUserSecurity;
    @Resource
    private IUserForbid iUserForbid;

    @Resource
    private IUserIpChange iUserIpChange;
    @Resource
    private MultiFactorVerifyService multiFactorVerifyService;
    @Resource
    private CertificateCenterDispatcher certificateCenterDispatcher;
    @Resource
    private UserSecurityResetHelper userSecurityResetHelper;

    private static final String CACHE_USER_IP_CONFIRM_RESULT = "USER_IP_CONFIRM_RESULT_%s";
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindGoogleVerifyResponse> unbindGoogleVerify(
            @Validated @RequestBody APIRequest<UnbindGoogleRequest> request) throws Exception {
        return this.iUserSecurity.unbindGoogleVerify(request);
    }

    // TODO 一期上线前功能屏蔽
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindGoogleVerifyV2Response> unbindGoogleVerifyV2(APIRequest<UnbindGoogleV2Request> request) throws Exception {
        return this.iUserSecurity.unbindGoogleVerifyV2(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindGoogleVerifyResponse> resetGoogleVerify(
            @Validated @RequestBody APIRequest<ResetGoogleRequest> request) throws Exception {
        return this.iUserSecurity.resetGoogleVerify(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<BindGoogleVerifyResponse> bindGoogleVerify(
            @Validated @RequestBody APIRequest<BindGoogleVerifyRequest> request) throws Exception {
        return this.iUserSecurity.bindGoogleVerify(request);
    }

    // TODO 一期上线前功能屏蔽
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<BindGoogleVerifyV2Response> bindGoogleVerifyV2(APIRequest<BindGoogleVerifyV2Request> request) throws Exception {
        return this.iUserSecurity.bindGoogleVerifyV2(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> bindMobile(@Validated @RequestBody APIRequest<BindMobileRequest> request)
            throws Exception {
        return this.iUserSecurity.bindMobile(request);
    }

    // TODO 一期上线前功能屏蔽
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> bindMobileV2(APIRequest<BindMobileV2Request> request) throws Exception {
        return this.iUserSecurity.bindMobileV2(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindMobileResponse> unbindMobile(
            @Validated @RequestBody APIRequest<UnbindMobileRequest> request) throws Exception {
        return this.iUserSecurity.unbindMobile(request);
    }

    // TODO 一期上线前功能屏蔽
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindMobileV2Response> unbindMobileV2(APIRequest<UnbindMobileV2Request> request) throws Exception {
        return this.iUserSecurity.unbindMobileV2(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> resetLoginFailedNum(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUserSecurity.resetLoginFailedNum(request);
    }

    @Override
    public APIResponse<Integer> updateUserSecurityByUserId(
            @Validated @RequestBody APIRequest<UpdateUserSecurityByUserIdRequest> request) throws Exception {
        return this.iUserSecurity.updateUserSecurityByUserId(request);
    }

    @Override
    public APIResponse<UserSecurityVo> getUserSecurityByUserId(
            @Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return this.iUserSecurity.getUserSecurityByUserId(request);
    }

    @Override
    public APIResponse<UserSecurityVo> getUserSecurityByEmail(
            @Validated @RequestBody APIRequest<UserEmailRequest> request) throws Exception {
        return this.iUserSecurity.getUserSecurityByEmail(request);
    }

    @Override
    public APIResponse<Boolean> isMobileExist(@Validated @RequestBody APIRequest<MobileRequest> request)
            throws Exception {
        return this.iUserSecurity.isMobileExist(request);
    }

    @Override
    public APIResponse<Boolean> isMobileExistRateLimit(@Validated @RequestBody APIRequest<MobileRateLimitRequest> request)
            throws Exception {
        return this.iUserSecurity.isMobileExistRateLimit(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<SendBindMobileVerifyCodeResponse> sendBindMobileVerifyCode(
            @Validated @RequestBody APIRequest<SendBindMobileVerifyCodeRequest> request) throws Exception {
        return this.iUserSecurity.sendBindMobileVerifyCode(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<SendBindEmailVerifyCodeResponse> sendBindEmailVerifyCode(@Validated @RequestBody APIRequest<SendBindEmailVerifyCodeRequest> request) throws Exception {
        SendBindEmailVerifyCodeRequest requestBody=request.getBody();
        SendBindEmailVerifyCodeResponse resp=iUserSecurity.sendBindEmailVerifyCode(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<BindEmailResponse> bindEmail(@Validated @RequestBody APIRequest<BindEmailRequest> request) throws Exception {
        BindEmailResponse resp=iUserSecurity.bindEmail(request);
        return APIResponse.getOKJsonResult(resp);
    }


    @Override
    public APIResponse<SendEmailVerifyCodeResponse> sendEmailVerifyCode(@Validated @RequestBody APIRequest<SendEmailVerifyCodeRequest> request) throws Exception {
        SendEmailVerifyCodeRequest requestBody=request.getBody();
        SendEmailVerifyCodeResponse resp=iUserSecurity.sendEmailVerifyCode(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> openMobileVerify(
            @Validated @RequestBody APIRequest<OpenOrCloseMobileVerifyRequest> request) throws Exception {
        return this.iUserSecurity.openMobileVerify(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> closeMobileVerify(
            @Validated @RequestBody APIRequest<OpenOrCloseMobileVerifyRequest> request) throws Exception {
        return this.iUserSecurity.closeMobileVerify(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> openBNBFee(@Validated @RequestBody APIRequest<OpenOrCloseBNBFeeRequest> request)
            throws Exception {
        return this.iUserSecurity.openBNBFee(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> closeBNBFee(@Validated @RequestBody APIRequest<OpenOrCloseBNBFeeRequest> request)
            throws Exception {
        return this.iUserSecurity.closeBNBFee(request);
    }

    @Override
    public APIResponse<Integer> openWithdrawWhiteStatus(
            @Validated @RequestBody APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request) throws Exception {
        return this.iUserSecurity.openWithdrawWhiteStatus(request);
    }

    @Override
    public APIResponse<OpenWithdrawWhiteStatusV2Response> openWithdrawWhiteStatusV2(@Validated @RequestBody APIRequest<OpenWithdrawWhiteStatusV2Request> request) throws Exception {
        return this.iUserSecurity.openWithdrawWhiteStatusV2(request);
    }

    @Override
    public APIResponse<CloseWithdrawWhiteStatusResponse> closeWithdrawWhiteStatus(
            @Validated @RequestBody APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request) throws Exception {
        return this.iUserSecurity.closeWithdrawWhiteStatus(request);
    }

    @Override
    public APIResponse<CloseWithdrawWhiteStatusV2Response> closeWithdrawWhiteStatusV2(@Validated @RequestBody APIRequest<CloseWithdrawWhiteStatusV2Request> request) throws Exception {
        return this.iUserSecurity.closeWithdrawWhiteStatusV2(request);
    }

    @Override
    public APIResponse<ConfirmCloseWithdrawWhiteStatusResponse> confirmCloseWithdrawWhiteStatus(
            @Validated @RequestBody APIRequest<ConfirmCloseWithdrawWhiteStatusRequest> request) throws Exception {
        return this.iUserSecurity.confirmCloseWithdrawWhiteStatus(request);
    }

    @Override
    public APIResponse<Integer> aouAntiPhishingCode(@Validated @RequestBody APIRequest<BindPhishingCodeRequest> request)
            throws Exception {
        return this.iUserSecurity.aouAntiPhishingCode(request);
    }

    @Override
    public APIResponse<Integer> aouAntiPhishingCodeV2(@Validated @RequestBody APIRequest<BindPhishingCodeV2Request> request) throws Exception {
        return this.iUserSecurity.aouAntiPhishingCodeV2(request);
    }

    @Override
    public APIResponse<Integer> forbiddenUser(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUserForbid.forbiddenUser(request);
    }

    @Override
    public APIResponse<Integer> forbiddenUserTotal(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUserForbid.forbiddenUserTotal(request);
    }

    @Override
    public APIResponse<Boolean> forbidByCode(@Validated @RequestBody APIRequest<UserForbidRequest> request) {
        return this.iUserForbid.forbidUserByCode(request);
    }

    @Override
    public APIResponse<String> generateForbidCode(@Validated @RequestBody APIRequest<UserIdRequest> request) {
        String disableCode = iUserSecurity.generateDisableCode(request.getBody().getUserId());
        return APIResponse.getOKJsonResult(disableCode);
    }

    @Override
    public APIResponse<CheckForbidCodeResponse> checkForbidCode(@RequestParam("code") String code) {
        CheckForbidCodeResponse rs = iUserSecurity.checkForbidCode(code);
        return APIResponse.getOKJsonResult(rs);
    }

    @Override
    public APIResponse<?> lockUser(@Validated @RequestBody APIRequest<UserLockRequest> request) throws Exception {
        return this.iUserSecurity.lockUser(request);
    }

    @Override
    public APIResponse<ConfirmedUserIpChangeResponse> confirmedUserIpChange(
            @Validated @RequestBody APIRequest<ConfirmedUserIpChangeRequest> request) throws Exception {
        String cacheKey = String.format(CACHE_USER_IP_CONFIRM_RESULT, request.getBody().getId());
        // 先从缓冲中取结果，解决重复点击的问题
        ConfirmedUserIpChangeResponse cache = RedisCacheUtils.get(cacheKey, ConfirmedUserIpChangeResponse.class);
        if (cache != null) {
            return APIResponse.getOKJsonResult(cache);
        }
        APIResponse<ConfirmedUserIpChangeResponse> rs = this.iUserIpChange.confirmedUserIpChange(request);
        if (rs.getStatus() == APIResponse.Status.OK) {
            RedisCacheUtils.set(cacheKey, rs.getData(), Constant.HOUR_HALF);
        }
        return rs;
    }

    @Override
    public APIResponse<Integer> setSecurityLevel(
            @Validated @RequestBody APIRequest<SecurityLevelSettingRequest> request) throws Exception {
        return this.iUserSecurity.setSecurityLevel(request);
    }

    @Override
    public APIResponse<List<String>> batchUpdateSecurityUserLevel(
            @Validated @RequestBody APIRequest<BatchUpdateSecurityLevelRequest> request) throws Exception {
        return this.iUserSecurity.batchUpdateSecurityUserLevel(request.getBody());
    }

    @Override
    public APIResponse<Integer> resetSecurity(@Validated @RequestBody APIRequest<ResetSecurityRequest> request)
            throws Exception {
        return this.iUserSecurity.resetSecurity(request);
    }

    @Override
    public APIResponse<?> oneButtonActivation(@Validated @RequestBody APIRequest<OneButtonActivationRequest> request)
            throws Exception {
        return this.iUserSecurity.oneButtonActivation(request);
    }

    @Override
    public APIResponse<?> oneButtonDisable(@Validated @RequestBody APIRequest<OneButtonDisableRequest> request)
            throws Exception {
        return this.iUserSecurity.oneButtonDisable(request);
    }

    @Override
    public APIResponse<?> disableTradeAndCancleOrder(@Validated @RequestBody APIRequest<OneButtonDisableRequest> request)
            throws Exception {
        return this.iUserSecurity.disableTradeAndCancleOrder(request);
    }

    @Override
    public APIResponse<?> resetActivationUser(@Validated @RequestBody APIRequest<ResetActivationUserRequest> request) {
        this.iUserSecurity.resetActivationUser(request.getBody());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<?> resetDisableTrading(@Validated @RequestBody APIRequest<ResetDisableTradingRequest> request) {
        userSecurityResetHelper.resetDisableTrading(request.getBody().getUserId());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<?> disableLogin(@Validated @RequestBody APIRequest<DisableLoginRequest> request)
            throws Exception {
        return this.iUserSecurity.disableLogin(request);
    }

    @Override
    public APIResponse<?> activeLogin(@Validated @RequestBody APIRequest<ActiveLoginRequest> request)
            throws Exception {
        return this.iUserSecurity.activeLogin(request);
    }

    @Override
    public APIResponse<String> selectAntiPhishingCode(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUserSecurity.selectAntiPhishingCode(request);
    }

    @Override
    public APIResponse<String> verificationsTwo(@Validated @RequestBody APIRequest<VarificationTwoRequest> request)
            throws Exception {
        VarificationTwoRequest body = request.getBody();
        this.iUserSecurity.verificationsTwo(body.getUserId(), body.getAuthType(), body.getCode(), body.getAutoDel());

        return APIResponse.getOKJsonResult(Constant.SUCCESS);
    }

    @Override
    public APIResponse<String> verificationsTwoV2(@Validated @RequestBody APIRequest<VerificationTwoV2Request> request)
            throws Exception {
        VerificationTwoV2Request body = request.getBody();
        this.iUserSecurity.verificationsTwoV2(body.getUserId(), body.getAuthType(), body.getCode(), body.getScenario(), body.getAutoDel());
        return APIResponse.getOKJsonResult(Constant.SUCCESS);
    }

    @Override
    public APIResponse<VerificationTwoV3Response> verificationsTwoV3(@Validated @RequestBody APIRequest<VerificationTwoV3Request> request) throws Exception {
        VerificationTwoV3Request body = request.getBody();

        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(body.getUserId())
                .bizScene(body.getBizScene())
                .emailVerifyCode(body.getEmailVerifyCode())
                .googleVerifyCode(body.getGoogleVerifyCode())
                .mobileVerifyCode(body.getMobileVerifyCode())
                .yubikeyVerifyCode(body.getYubikeyVerifyCode())
                .build();
        this.iUserSecurity.verifyMultiFactors(verify);
        return APIResponse.getOKJsonResult(new VerificationTwoV3Response());
    }

    @Override
    public APIResponse<VerificationsDemandResponse> verificationsDemand(@Validated @RequestBody APIRequest<VerificationsDemandRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.verificationsDemand(request.getBody()));
    }

    @Override
    public APIResponse<GetVerificationTwoCheckListResponse> getVerificationTwoCheckList(@Validated @RequestBody APIRequest<GetVerificationTwoCheckListRequest> request) throws Exception {
        GetVerificationTwoCheckListRequest body = request.getBody();

        MultiFactorSceneCheckQuery query = MultiFactorSceneCheckQuery.builder()
                .userId(body.getUserId())
                .bizScene(body.getBizScene())
                .flowId(body.getFlowId())
                .clientType(request.getTerminal().getCode())
                .deviceInfo(body.getDeviceInfo())
                .build();
        MultiFactorSceneCheckResult checkResult = this.iUserSecurity.getVerificationTwoCheckList(query);
        GetVerificationTwoCheckListResponse resp = new GetVerificationTwoCheckListResponse();
        resp.setNeedCheckVerifyList(checkResult.getNeedCheckVerifyList());
        resp.setNeedBindVerifyList(checkResult.getNeedBindVerifyList());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<UserSecurityListResponse> selectUserSecurityList(
            @Validated @RequestBody APIRequest<GetUserListRequest> request) {
        return this.iUserSecurity.selectUserSecurityList(request);
    }

    @Override
    @SecurityLog(name = "修改提币禁用状态", operateType = AccountConstants.UPDATE_WITHDRAWSECURITYSTATUS,
            userId = "#request.body.userId")
    public APIResponse<Integer> updateStatusByUserId(@Validated @RequestBody APIRequest<SecurityStatusRequest> request)
            throws Exception {
        return this.iUserSecurity.updateStatusByUserId(request);
    }

    @Override
    public APIResponse<Integer> updateWithdrawStatusByUserId(@Validated @RequestBody APIRequest<UpdateWithdrawStatusRequest> request)
            throws Exception {
        return this.iUserSecurity.updateWithdrawStatusByUserId(request);
    }


    @Override
    public APIResponse<Long> getPasswordUpdateTime(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.getPasswordUpdateTime(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<AccountUpdateTimeForTrade> getAccountUpdateTimeForTrade(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.getAccountUpdateTimeForTrade(request.getBody().getUserId()));
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<GoogleAuthKeyResp> generateAuthKeyAndQrCode(
            @Validated @RequestBody APIRequest<UserIdRequest> request) {
        return APIResponse.getOKJsonResult(iUserSecurity.generateAuthKeyAndQrCode(request.getBody()));
    }

    @Override
    public APIResponse<Long> get2FaUnbindTime(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.get2FaUnbindTime(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<Integer> changeWithdrawSecurityFaceStatus(@Validated @RequestBody APIRequest<SecurityFaceStatusRequest> request) {
        return APIResponse.getOKJsonResult(this.certificateCenterDispatcher.changeWithdrawSecurityFaceStatus(request.getBody()));
    }

    @Override
    public APIResponse<Integer> changeWithdrawFaceCheckStatus(@Validated @RequestBody APIRequest<WithdrawFaceStatusChangeRequest> request) {
        return APIResponse.getOKJsonResult(iUserSecurity.changeWithdrawFaceCheckStatus(request.getBody()));
    }

    @Override
    public APIResponse<Boolean> getReBindGoogleVerifyStatus(@Validated @RequestBody APIRequest<GetReBindGoogleVerifyStatusRequest> request) {
        return APIResponse.getOKJsonResult(this.iUserSecurity.getReBindGoogleVerifyStatus(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<Boolean> isSecurityKeyEnabledInSpecifiedScenario(@Validated @RequestBody APIRequest<IsSecurityKeyEnabledRequest> request) {
        return APIResponse.getOKJsonResult(this.iUserSecurity.isYubikeyEnabledInSpecifiedScenario(request.getBody().getUserId(), request.getBody().getScenario()));
    }

    @Override
    public APIResponse<Void> updateSecurityKeyApplicationScenarios(@Validated @RequestBody APIRequest<UpdateSecurityKeyApplicationScenarioRequest> request) {
        this.iUserSecurity.updateYubikeyEnableScenarios(request.getBody().getUserId(), request.getBody().getScenarios(), request.getBody().getCode());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<?> disableForFiat(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return this.iUserSecurity.disableForFiat(request);
    }

    @Override
    public APIResponse<WithdrawTimeForTradeResponse> getWithdrawTimeForTrade(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.getWithdrawTimeForTrade(request.getBody().getUserId()));
    }

    @Override
    @SecurityLog(name = "开启快速提币", operateType = AccountConstants.ENABLE_FAST_WITHDRAW_SWITCH,
            userId = "#request.body.userId")
    public APIResponse<EnableFastWithdrawSwitchResponse> enableFastWithdrawSwitch(@Validated @RequestBody APIRequest<EnableFastWithdrawSwitchRequest> request) throws Exception {
        EnableFastWithdrawSwitchRequest requestBody=request.getBody();
        EnableFastWithdrawSwitchResponse enableFastWithdrawSwitchResponse= iUserSecurity.enableFastWithdrawSwitch(requestBody);
        return APIResponse.getOKJsonResult(enableFastWithdrawSwitchResponse);
    }

    @Override
    @SecurityLog(name = "关闭快速提币", operateType = AccountConstants.DISABLE_FAST_WITHDRAW_SWITCH,
            userId = "#request.body.userId")
    public APIResponse<DisableFastWithdrawSwitchResponse> disableFastWithdrawSwitch(@Validated @RequestBody APIRequest<DisableFastWithdrawSwitchRequest> request) throws Exception {
        DisableFastWithdrawSwitchRequest requestBody=request.getBody();
        DisableFastWithdrawSwitchResponse disableFastWithdrawSwitchResponse= iUserSecurity.disableFastWithdrawSwitch(requestBody);
        return APIResponse.getOKJsonResult(disableFastWithdrawSwitchResponse);
    }


    @Override
    public APIResponse<EnableFundPasswordResponse> enableFundPassword(@Validated @RequestBody APIRequest<EnableFundPasswordRequest> request) throws Exception {
        EnableFundPasswordRequest requestBody=request.getBody();
        EnableFundPasswordResponse enableFundPasswordResponse= iUserSecurity.enableFundPassword(requestBody);
        return APIResponse.getOKJsonResult(enableFundPasswordResponse);
    }

    @Override
    public APIResponse<DisableFundPasswordResponse> disableFundPassword(@Validated @RequestBody APIRequest<DisableFundPasswordRequest> request) throws Exception {
        DisableFundPasswordRequest requestBody=request.getBody();
        DisableFundPasswordResponse disableFundPasswordResponse= iUserSecurity.disableFundPassword(requestBody);
        return APIResponse.getOKJsonResult(disableFundPasswordResponse);
    }

    @Override
    public APIResponse<ResetFundPasswordResponse> resetFundPassword(@Validated @RequestBody APIRequest<ResetFundPasswordRequest> request) throws Exception {
        ResetFundPasswordRequest requestBody=request.getBody();
        ResetFundPasswordResponse resetFundPasswordResponse= iUserSecurity.resetFundPassword(requestBody);
        return APIResponse.getOKJsonResult(resetFundPasswordResponse);
    }

    @Override
    public APIResponse<VerifyFundPasswordResponse> verifyFundPassword(@Validated @RequestBody APIRequest<VerifyFundPasswordRequest> request) throws Exception {
        VerifyFundPasswordRequest requestBody=request.getBody();
        VerifyFundPasswordResponse verifyFundPasswordResponse= iUserSecurity.verifyFundPassword(requestBody);
        return APIResponse.getOKJsonResult(verifyFundPasswordResponse);
    }

    @Override
    public APIResponse<GetUserEmailAndMobileByUserIdResponse> getUserEmailAndMobileByUserId(@Validated @RequestBody APIRequest<GetUserEmailAndMobileByUserIdRequest> request) throws Exception {
        GetUserEmailAndMobileByUserIdRequest requestBody=request.getBody();
        GetUserEmailAndMobileByUserIdResponse resp= iUserSecurity.getUserEmailAndMobileByUserId(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<GetUserIdByEmailOrMobileResponse> getUserIdByMobileOrEmail(@Validated @RequestBody APIRequest<GetUserIdByEmailOrMobileRequest> request) throws Exception {
        GetUserIdByEmailOrMobileRequest requestBody=request.getBody();
        GetUserIdByEmailOrMobileResponse resp= iUserSecurity.getUserIdByMobileOrEmail(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<AccountForgotPasswordPreCheckResponse> forgotPasswordPreCheck(@Validated @RequestBody APIRequest<AccountForgotPasswordPreCheckRequest> request) throws Exception {
        AccountForgotPasswordPreCheckResponse resp= iUserSecurity.forgotPasswordPreCheck(request);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<AccountResetPasswordResponseV2> resetPasswordV2(@Validated @RequestBody APIRequest<AccountResetPasswordRequestV2> request) throws Exception {
        AccountResetPasswordRequestV2 requestBody=request.getBody();
        AccountResetPasswordResponseV2 resp= iUserSecurity.resetPasswordV2(requestBody);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<MultiFactorSceneCheckResponse> get2FaVerifyList(APIRequest<MultiFactorSceneCheckRequest> request) {
        UserTwoVerifyInfo unbindMobileArg = new UserTwoVerifyInfo();
        BeanUtils.copyProperties(request.getBody(), unbindMobileArg);
        MultiFactorSceneCheckResult result = multiFactorVerifyService.get2FaVerifyList(unbindMobileArg);
        MultiFactorSceneCheckResponse response = new MultiFactorSceneCheckResponse();
        BeanUtils.copyProperties(result, response);
        return APIResponse.getOKJsonResult(response);
    }


    @Override
    public APIResponse<GetUser2faResponse> getUser2fa(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserSecurity.getUser2fa(request.getBody()));
    }

    @Override
    public APIResponse<UserRiskInfoResponse> getUserRiskInfo(@RequestParam("userId") Long userId) {
        return APIResponse.getOKJsonResult(iUserSecurity.getUserRiskInfo(userId));
    }

    @Override
    public APIResponse<ChangeEmailResponse> changeEmail(@Validated @RequestBody APIRequest<ChangeEmailRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.changeEmail(request.getBody()));
    }

    @Override
    public APIResponse<ChangeMobileResponse> changeMobile(@Validated @RequestBody  APIRequest<ChangeMobileRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.changeMobile(request.getBody()));
    }

    @Override
    public APIResponse<OldWithdrawDailyLimitModifyVo> getOldWithdrawDailyLimitModify(@RequestParam("userId") Long userId) {
        OldWithdrawDailyLimitModify modify = userSecurityResetHelper.getOldWithdrawDailyLimitModify(userId);
        OldWithdrawDailyLimitModifyVo vo = null;
        if (modify != null) {
            vo = new OldWithdrawDailyLimitModifyVo();
            BeanUtils.copyProperties(modify, vo);
        }
        return APIResponse.getOKJsonResult(vo);
    }

    @Override
    public APIResponse<Map<Long, String>> getOldWithdrawDailyLimitModifyCause(@Validated @RequestBody APIRequest<GetUserListRequest> request) {
        Map<Long, String> resultMap =userSecurityResetHelper.oldWithdrawDailyLimitModifyCause(request.getBody().getUserIds());
        return APIResponse.getOKJsonResult(resultMap);
    }

    @Override
    public APIResponse<GetCapitalWithdrawVerifyParamResponse> getCapitalWithdrawVerfiyParam(@Validated @RequestBody APIRequest<GetCapitalWithdrawVerifyParamRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserSecurity.getCapitalWithdrawVerfiyParam(request.getBody()));

    }
}
