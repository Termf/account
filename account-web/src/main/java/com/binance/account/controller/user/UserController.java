package com.binance.account.controller.user;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Validator;

import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.aop.MarginValidate;
import com.binance.account.api.UserApi;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constants.AccountConstants;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserFuture;
import com.binance.account.service.user.IUserLVT;
import com.binance.account.vo.security.request.CountAgentNumberRequest;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.FastCreateFutureAccountRequest;
import com.binance.account.vo.security.request.MarginRelationShipRequest;
import com.binance.account.vo.security.request.ReCaptchaReq;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.UserIpLikeVo;
import com.binance.account.vo.user.UserIpVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.response.*;
import com.binance.master.commons.SearchResult;
import com.binance.account.vo.user.request.*;
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
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.ValidatorUtils;
import com.binance.master.validator.groups.Add;
import com.binance.master.validator.groups.Auth;
import com.binance.master.validator.groups.Edit;
import com.binance.master.validator.groups.Select;
import com.binance.master.validator.groups.Update;
import com.binance.platform.resilience4j.ratelimiter.ServerRateLimiter;
import com.binance.platform.resilience4j.ratelimiter.UserIdAndIPRateLimiterStrategy;

import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class UserController implements UserApi {

    @Resource
    private IUser iUser;

    @Resource
    private Validator validator;


    @Autowired
    private IUserFuture userFuture;
    
    @Autowired
    private IUserLVT iUserLVT;


    @Value("${auto.create.c2c.account.switch:false}")
    private boolean autoCreateC2CAccountSwitch;


    @Override
    public APIResponse<RegisterUserResponse> register(
            @Validated(value = Add.class) @RequestBody() APIRequest<RegisterUserRequest> request) throws Exception {
        if(request.getBody().getIsFastCreatFuturesAccountProcess().booleanValue() && StringUtils.isNotBlank(request.getBody().getFuturesReferalCode())){
            log.info("fastCreateFutureAccount register check , email:{}", request.getBody().getEmail());
            FastCreateFutureAccountRequest fastCreateFutureAccountRequest=new FastCreateFutureAccountRequest();
            BeanUtils.copyProperties(request.getBody(),fastCreateFutureAccountRequest);
            userFuture.validateCretaeFutureAccount(fastCreateFutureAccountRequest);
            //走期货一件开户的流程需要把现货的推荐人全部设置为空
            request.getBody().setAgentId(null);
            request.getBody().setAgentRateCode(null);
        }
        return this.iUser.register(request);
    }

    @Override
    public APIResponse<RegisterUserResponseV2> registerV2(@Validated(value = Add.class) @RequestBody() APIRequest<RegisterUserRequestV2> request) throws Exception {
        if(request.getBody().getIsFastCreatFuturesAccountProcess().booleanValue() && StringUtils.isNotBlank(request.getBody().getFuturesReferalCode())){
            log.info("fastCreateFutureAccountV2 register check , email:{}", request.getBody().getEmail());
            FastCreateFutureAccountRequest fastCreateFutureAccountRequest=new FastCreateFutureAccountRequest();
            BeanUtils.copyProperties(request.getBody(),fastCreateFutureAccountRequest);
            userFuture.validateCretaeFutureAccount(fastCreateFutureAccountRequest);
            //走期货一件开户的流程需要把现货的推荐人全部设置为空
            request.getBody().setAgentId(null);
            request.getBody().setAgentRateCode(null);
        }
        return this.iUser.registerV2(request);
    }

    @Override
    @Monitored
    public APIResponse<LoginUserResponse> login(@RequestBody() APIRequest<LoginUserRequest> request,
            BindingResult errors) throws Exception {
        LoginUserRequest requestBody = request.getBody();
        boolean bo = true;
        if (requestBody.getIsAuth() != null && requestBody.getIsAuth()) {
            bo = ValidatorUtils.validate(request, errors, Auth.class);
        } else {
            bo = ValidatorUtils.validate(request, errors, Select.class);
            request.getBody().setAuthType(null);
        }
        if (!bo) {
            throw new BindException(errors);
        }
        return this.iUser.login(request);
    }

    @Override
    public APIResponse<LoginUserResponseV2> loginV2(@RequestBody() APIRequest<LoginUserRequestV2> request, BindingResult errors) throws Exception {
        LoginUserRequestV2 requestBody = request.getBody();
        boolean bo = true;
        if (requestBody.getIsAuth() != null && requestBody.getIsAuth()) {
            bo = ValidatorUtils.validate(request, errors, Auth.class);
        } else {
            bo = ValidatorUtils.validate(request, errors, Select.class);
        }
        if (!bo) {
            throw new BindException(errors);
        }
        return this.iUser.loginV2(request);
    }

    @Override
    public APIResponse<UpdatePwdUserResponse> updatePwd(
            @Validated(value = Edit.class) @RequestBody() APIRequest<UpdatePwdUserRequest> request) throws Exception {
        return this.iUser.updatePwd(request);
    }

    @Override
    public APIResponse<UpdatePwdUserV2Response> updatePwdV2(
            @Validated(value = Edit.class) @RequestBody() APIRequest<UpdatePwdUserV2Request> request) throws Exception {
        return this.iUser.updatePwdV2(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<UpdateAccountUserResponse> updateAccount(
            @Validated(value = Edit.class) @RequestBody() APIRequest<UpdateAccountUserRequest> request)
            throws Exception {
        return this.iUser.updateAccount(request);
    }
    @MarginValidate
    @Override
    public APIResponse<AccountActiveUserResponse> accountActive(
            @Validated(value = Edit.class) @RequestBody() APIRequest<AccountActiveUserRequest> request)
            throws Exception {
        APIResponse<AccountActiveUserResponse> responseAPIResponse=this.iUser.accountActive(request);
        AccountActiveUserResponse accountActiveUserResponse=responseAPIResponse.getData();
        //判断是否要开通期货账户
        String fastCreateFutureAccount=iUser.getConfigByConfigType(accountActiveUserResponse.getUserId(),AccountConstants.FAST_CREATE_FUTURE_ACCOUNT);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(fastCreateFutureAccount)){
            //开启期货账户
            String futuresReferalCode=iUser.getConfigByConfigType(accountActiveUserResponse.getUserId(),AccountConstants.FUTURES_REFERAL_CODE);
            //组装请求参数
            APIRequest<CreateFutureAccountRequest> originRequest = new APIRequest<CreateFutureAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            CreateFutureAccountRequest createFutureAccountRequest=new CreateFutureAccountRequest();
            createFutureAccountRequest.setUserId(accountActiveUserResponse.getUserId());
            createFutureAccountRequest.setIsFastCreatFuturesAccountProcess(true);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(futuresReferalCode)) {
                createFutureAccountRequest.setAgentCode(futuresReferalCode);
            }
            HintManager hintManager = null;
            try {
                hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
                APIResponse<CreateFutureUserResponse> apiResponse=userFuture.createFutureAccount(APIRequest.instance(originRequest, createFutureAccountRequest));
            } finally {
                if (null != hintManager) {
                    hintManager.close();
                }
            }
        }

        // 激活默认开通法币账号
        if(autoCreateC2CAccountSwitch){
            try (HintManager createFiatHintManager = HintManager.getInstance()) {
                createFiatHintManager.setMasterRouteOnly();

                UserIdRequest createFiatRequest = new UserIdRequest();
                createFiatRequest.setUserId(accountActiveUserResponse.getUserId());
                iUser.createFiatAccount(APIRequest.instance(createFiatRequest), true);
            } catch (Exception e) {
                log.warn("UserController.accountActive,createFiatAccount error", e);
            }
        }

        return responseAPIResponse;
    }

    @MarginValidate
    @Override
    public APIResponse<AccountActiveUserV2Response> accountActiveV2(APIRequest<AccountActiveUserV2Request> request) throws Exception {
        APIResponse<AccountActiveUserV2Response> responseAPIResponse = this.iUser.accountActiveV2(request);
        AccountActiveUserV2Response accountActiveUserResponse = responseAPIResponse.getData();
        //判断是否要开通期货账户
        String fastCreateFutureAccount=iUser.getConfigByConfigType(accountActiveUserResponse.getUserId(),AccountConstants.FAST_CREATE_FUTURE_ACCOUNT);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(fastCreateFutureAccount)){
            //开启期货账户
            String futuresReferalCode=iUser.getConfigByConfigType(accountActiveUserResponse.getUserId(),AccountConstants.FUTURES_REFERAL_CODE);
            //组装请求参数
            APIRequest<CreateFutureAccountRequest> originRequest = new APIRequest<CreateFutureAccountRequest>();
            originRequest.setLanguage(LanguageEnum.ZH_CN);
            originRequest.setTerminal(TerminalEnum.WEB);
            originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
            CreateFutureAccountRequest createFutureAccountRequest=new CreateFutureAccountRequest();
            createFutureAccountRequest.setUserId(accountActiveUserResponse.getUserId());
            createFutureAccountRequest.setIsFastCreatFuturesAccountProcess(true);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(futuresReferalCode)) {
                createFutureAccountRequest.setAgentCode(futuresReferalCode);
            }
            HintManager hintManager = null;
            try {
                hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
                APIResponse<CreateFutureUserResponse> apiResponse=userFuture.createFutureAccount(APIRequest.instance(originRequest, createFutureAccountRequest));
            }catch (Exception e) {
                log.warn("UserController.accountActive,createFutureAccount error", e);
            } finally {
                if (null != hintManager) {
                    hintManager.close();
                }
            }
        }

        // 激活默认开通法币账号
        if(autoCreateC2CAccountSwitch){
            try (HintManager createFiatHintManager = HintManager.getInstance()) {
                createFiatHintManager.setMasterRouteOnly();

                UserIdRequest createFiatRequest = new UserIdRequest();
                createFiatRequest.setUserId(accountActiveUserResponse.getUserId());
                iUser.createFiatAccount(APIRequest.instance(createFiatRequest), true);
            } catch (Exception e) {
                log.warn("UserController.accountActive,createFiatAccount error", e);
            }
        }
        return responseAPIResponse;
    }

    @Override
    public APIResponse<GetUserResponse> getUserByEmail(@Validated @RequestBody APIRequest<GetUserRequest> request)
            throws Exception {
        return this.iUser.getUserByEmail(request);
    }
    @Override
    public APIResponse<GetUserResponse> getUserById(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return this.iUser.getUserById(request);
    }

    @Override
    public APIResponse<GetUserResponse> getUserByIdFromMasterDb(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        HintManager hintManager = HintManager.getInstance();
        hintManager.setMasterRouteOnly();
        return this.iUser.getUserById(request);
    }

    @Override
    public APIResponse<AccountForgotPasswordResponse> forgotPasswordSendEmail(
            @Validated @RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception {
        return this.iUser.forgotPasswordSendEmail(request);
    }
    @MarginValidate(email = "#request.body.email")
    @Override
    public APIResponse<AccountResetPasswordResponse> resetPassword(
            @Validated @RequestBody APIRequest<AccountResetPasswordRequest> request) throws Exception {
        return this.iUser.resetPassword(request);
    }
    //@MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> enableUserStatus(@Validated @RequestBody APIRequest<UserStatusRequest> request)
            throws Exception {
        return this.iUser.enableUserStatus(request);
    }
    //@MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> disableUserStatus(@Validated @RequestBody APIRequest<UserStatusRequest> request)
            throws Exception {
        return this.iUser.disableUserStatus(request);
    }

    @Override
    @MarginValidate(email = "#request.body.email")
    public APIResponse<Integer> updateUserByEmail(@Validated @RequestBody APIRequest<UpdateUserByEmailRequest> request)
            throws Exception {
        return this.iUser.updateUserByEmail(request);
    }
    @MarginValidate(email = "#request.body.email")
    @Override
    public APIResponse<ResendSendActiveCodeResponse> resendSendActiveCode(
            @Validated(Update.class) @RequestBody() APIRequest<ResendSendActiveCodeRequest> request) throws Exception {
        return this.iUser.resendSendActiveCode(request);
    }
    @MarginValidate
    @Override
    public APIResponse<SendSmsAuthCoderResponse> sendSmsAuthCode(
            @Validated() @RequestBody() APIRequest<SendSmsAuthCoderRequest> request) throws Exception {
        return this.iUser.sendSmsAuthCode(request);
    }

    @MarginValidate
    @Override
    public APIResponse<SendSmsAuthCodeV2Response> sendSmsAuthCodeV2(APIRequest<SendSmsAuthCodeV2Request> request) throws Exception {
        return this.iUser.sendSmsAuthCodeV2(request);
    }

    @Override
    public APIResponse<GetUserEmailResponse> getUserEmailByUserId(
            @Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return this.iUser.getUserEmailByUserId(request);
    }

    @Override
    public APIResponse<GetUserEmailsResponse> getUserEmailByUserIds(
            @Validated @RequestBody APIRequest<GetUserListRequest> request) throws Exception {
        return this.iUser.getUserEmailByUserIds(request);
    }

    @Override
    public APIResponse<GetUserEmailsResponse> getUserIdByEmails(@RequestBody APIRequest<GetUserIdListRequest> request)throws Exception{
        return this.iUser.getUserIdsByEmails(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Boolean> passwordCheck(@Validated @RequestBody APIRequest<PasswordVerifyRequest> request)
            throws Exception {
        return this.iUser.passwordCheck(request);
    }

    @Override
    public APIResponse<SearchUserListResponse> searchUserList(
            @Validated @RequestBody APIRequest<SearchUserListRequest> request) throws Exception {
        return this.iUser.searchUserList(request);
    }

    @Override
    public APIResponse<BaseDetailResponse> baseDetail(@Validated @RequestBody APIRequest<BaseDetailRequest> request)
            throws Exception {
        return this.iUser.baseDetail(request);
    }

    @Override
    public APIResponse<UserBriefInfoResponse> briefInfo(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return this.iUser.briefInfo(request);
    }

    @Override
    public APIResponse<Long> getUserIdByEmail(@Validated @RequestBody APIRequest<GetUserRequest> request)
            throws Exception {
        return this.iUser.getUserIdByEmail(request);
    }

    @Override
    public APIResponse<GetUserListResponse> getUserListByUserIds(
            @Validated @RequestBody APIRequest<GetUserListRequest> request) throws Exception {
        return this.iUser.getUserListByUserIds(request);
    }

    @Override
    public APIResponse<Long> getUserIdByTradingAccount(
            @Validated @RequestBody APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception {
        return this.iUser.getUserIdByTradingAccount(request);
    }
    @MarginValidate(email = "#request.body.email")
    @Override
    public APIResponse<AccountForgotPasswordResponse> sendAppForgotPasswordEmail(
            @RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception {
        return iUser.sendAppForgotPasswordEmail(request);
    }
    @MarginValidate(email = "#request.body.email")
    @Override
    public APIResponse<AccountResetPasswordVerifyResponse> resetAppPasswordVerify(
            @Validated @RequestBody APIRequest<AccountResetPasswordVerifyRequest> request) throws Exception {
        return this.iUser.resetAppPasswordVerify(request);
    }
    @MarginValidate(email = "#request.body.email")
    @Override
    public APIResponse<AccountResetPasswordResponse> resetAppPassword(
            @RequestBody APIRequest<AccountResetPasswordRequest> request) throws Exception {
        return iUser.resetAppPassword(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> modifyUser(@Validated @RequestBody APIRequest<ModifyUserRequest> request)
            throws Exception {
        return iUser.modifyUser(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> updateNickName(@Validated @RequestBody APIRequest<UpdateNickNameRequest> request)
            throws Exception {
        return iUser.updateNickName(request);
    }

    @Override
    public APIResponse<Integer> modifyUserEmail(@Validated @RequestBody APIRequest<ModifyUserEmailRequest> request)
            throws Exception {
        return iUser.modifyUserEmail(request);
    }

    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<Integer> deleteUser(@Validated @RequestBody APIRequest<DeleteUserRequest> request)
            throws Exception {
        return iUser.deleteUser(request);
    }

    @Override
    public APIResponse<Boolean> isExist(@Validated @RequestBody APIRequest<String> request) throws Exception {
        return this.iUser.isExist(request);
    }

    @Override
    public APIResponse<Boolean> ipIsExist(@Validated @RequestBody APIRequest<UserIpRequest> request) throws Exception {
        return this.iUser.ipIsExist(request);
    }

    @Override
    public APIResponse<List<KycValidateResponse>> kycValidateInfo(
            @Validated @RequestBody APIRequest<BaseDetailRequest> request) throws Exception {
        return this.iUser.kycValidateInfo(request);
    }

    @Override
    public APIResponse<Integer> todayRegist(@Validated @RequestBody APIRequest<UserRegistRequest> request)
            throws Exception {
        return this.iUser.todayRegist(request);
    }

    @Override
    public APIResponse<Integer> saveUserIp(@Validated @RequestBody APIRequest<UserIpRequest> request) throws Exception {
        return this.iUser.saveUserIp(request);
    }

    @Override
    public APIResponse<GetUserResponse> getUserByMobile(@Validated @RequestBody APIRequest<UserMobileRequest> request)
            throws Exception {
        return this.iUser.getUserByMobile(request);
    }
    @MarginValidate(userId = "#request.body.userId")
    @Override
    public APIResponse<AccountActiveUserResponse> internalAccountActive(
            @RequestBody() APIRequest<UserIdRequest> request) throws Exception {
        return this.iUser.internalAccountActive(request);
    }

    @Override
    public APIResponse<UserIpLikeVo> ipLikeCheck(@Validated @RequestBody APIRequest<UserIpLikeVo> request)
            throws Exception {
        return iUser.ipLikeCheck(request);
    }

    @Override
    public APIResponse<List<UserIpVo>> getUserIpList(@Validated @RequestBody APIRequest<UserIdRequest> request) {
        return APIResponse.getOKJsonResult(iUser.getUserIpList(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<Boolean> fixVNUser(@RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return iUser.fixVNUser(request);
    }

    @Override
    public APIResponse<UserStatusEx> getUserStatusByUserId(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return iUser.getUserStatusByUserId(request);
    }

    @Override
    public APIResponse<Integer> saveRecaptcha(@RequestBody APIRequest<ReCaptchaReq> request) throws Exception {
        return iUser.saveRecaptcha(request);
    }

    @Override
    public APIResponse<Long> countAgentNumber(@RequestBody APIRequest<CountAgentNumberRequest> request) throws Exception {
        return iUser.countAgentNumber(request);
    }

    @Override
    public APIResponse<CreateMarginUserResponse> createMarginAccount(@RequestBody APIRequest<CreateMarginAccountRequest> request) throws Exception {
        return iUser.createMarginAccount(request);
    }

    @Override
    public APIResponse<CreateFiatUserResponse> createFiatAccount(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception {
        return iUser.createFiatAccount(request, false);
    }

    @Override
    public APIResponse<Boolean> isMarginRelationShip(@RequestBody APIRequest<MarginRelationShipRequest> request) throws Exception {
        return iUser.isMarginRelationShip(request);
    }

    @Override
    public APIResponse<List<GetBatchUserTypeListResponse>> getBatchUserTypeList(@RequestBody APIRequest<GetBatchUserTypeListRequest> request) throws Exception {
        return iUser.getBatchUserTypeList(request);
    }

    @Override
    public APIResponse<Long> getAccountIdByRootUserId(@Validated @RequestBody APIRequest<GetAccountIdByRootUserIdRequest> request) throws Exception {
        return iUser.getAccountIdByRootUserId(request);
    }

    @Override
    public APIResponse<List<GetUserResponse>> getUserAgentDetail(@Validated @RequestBody APIRequest<GetUserAgentDetailRequest> request) throws Exception {
        return iUser.getUserAgentDetail(request);
    }

    @Override
    public APIResponse<FuzzyMatchUserIndexResponse> fuzzyMatchUserIndex(@Validated @RequestBody APIRequest<FuzzyMatchUserIndexRequest> request) throws Exception {
        return iUser.fuzzyMatchUserIndex(request);
    }

    @Override
    public APIResponse<FuzzyMatchUserInfoResponse> fuzzyMatchUserInfo(@Validated @RequestBody APIRequest<FuzzyMatchUserInfoRequest> request) throws Exception {
        return iUser.fuzzyMatchUserInfo(request);
    }

    @Override
    public APIResponse<GetUserCommissionDetailResponse> getUserCommissionDetail(@Validated @RequestBody APIRequest<GetUserAgentDetailRequest> request) throws Exception {
        return iUser.getUserCommissionDetail(request);
    }

    @Override
    public APIResponse<SpecialUserIdResponse> getSpecialUserIds() throws Exception {
        return iUser.getSpecialUserIds();
    }

    @Override
    public APIResponse<UserTypeResponse> getUserTypeByUserId(@RequestBody() APIRequest<UserIdReq> request) throws Exception {
        return iUser.getUserTypeByUserId(request);
    }

    @Override
    public APIResponse<String> createUserAgentRate(@RequestBody @Validated APIRequest<UserAgentRateReq> request)
            throws Exception {
        return iUser.createUserAgentRate(request);
    }

    @Override
    public APIResponse<UserAgentRateResponse> getUserAgentRateByCode(@RequestBody @Validated APIRequest<String> request)
            throws Exception {
        return iUser.getUserAgentRateByCode(request);
    }

    @Override
    @SentinelResource(value = "/user/getUserPromoteByUserId")
    public APIResponse<SearchResult<GetUserAgentStatResponse>> getUserPromoteByUserId(
            @RequestBody APIRequest<UserAgentLinkReq> request) throws Exception {
        return iUser.getUserPromoteByUserId(request);
    }

    @Override
    public APIResponse updateLabelByAgentCode(@RequestBody @Validated APIRequest<UpdateAgentRateReq> request)
            throws Exception {
        return iUser.updateLabelByAgentCode(request);
    }

    @Override
    public APIResponse<GetUserAgentStatResponse> getOldPromoteByUserId(@RequestBody @Validated APIRequest<Long> request)
            throws Exception {
        return iUser.getOldPromoteByUserId(request);
    }

    @Override
    @SentinelResource(value = "/user/getReferralUserEmail")
    public APIResponse<SearchResult<String>> getReferralUserEmail(
            @RequestBody @Validated APIRequest<GetReferralEmailRequest> request) throws Exception {
        return iUser.getReferralUserEmail(request);
    }

    @Override
    public APIResponse<Integer> getRemainingAgentLinkNum(@RequestBody @Validated APIRequest<Long> request)
            throws Exception {
        return iUser.getRemainingAgentLinkNum(request);
    }

    @Override
    public APIResponse<Long> checkTestnetEmailIfPassKyc(@Validated @RequestBody APIRequest<GetUserRequest> request) throws Exception {
        Long userId=iUser.checkTestnetEmailIfPassKyc(request.getBody().getEmail());
        return APIResponse.getOKJsonResult(userId);
    }


    @Override
    public APIResponse<Integer> updateOrderConfirmStatus(@RequestBody APIRequest<OrderConfrimStatusRequest> request)
            throws Exception {
        return this.iUser.updateOrderConfirmStatus(request);
    }

    @Override
    public APIResponse<Boolean> saveOrupdateUserAgentConfig(
            @Validated @RequestBody APIRequest<UserAgentConfigReq> request) throws Exception {
        return iUser.saveOrupdateUserAgentConfig(request);
    }

    @Override
    public APIResponse<SearchResult<GetUserAgentConfigResponse>> selectUserAgentConfig(
            @Validated @RequestBody APIRequest<GetUserAgentConfigRequest> request) throws Exception {
        return iUser.selectUserAgentConfig(request);
    }

    @Override
    public APIResponse<UserStatusEx> getUserStatusByAgentCode(@Validated @RequestBody APIRequest<String> request)
            throws Exception {
        return iUser.getUserStatusByAgentCode(request);
    }

    @Override
    public APIResponse<Boolean> selectOneAsShareCode(@Validated @RequestBody APIRequest<UserAgentSelectShareReq> request)
            throws Exception {
        return iUser.selectOneAsShareCode(request);
    }

    @Override
    public APIResponse<Boolean> saveOrupdateSnapshotShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception {
        return iUser.saveOrupdateSnapshotShareConfig(request);
    }


    @Override
    public APIResponse<Boolean> deleteSnapshotShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception {
        return iUser.deleteSnapshotShareConfig(request);
    }

    @Override
    public APIResponse<SnapshotShareConfigsRes> selectAllSnapShareConfig(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request)
            throws Exception {
        return iUser.selectAllSnapShareConfig(request);
    }

    @Override
    @SentinelResource(value = "/user/selectCheckedShareCodeByUserId")
    public APIResponse<GetUserAgentStatResponse> selectCheckedShareCodeByUserId(@Validated @RequestBody APIRequest<Long> request)
            throws Exception {
        return iUser.selectCheckedShareCodeByUserId(request);
    }

    @Override
    public APIResponse<List<SnapshotShareConfigRes>> selectAllSnapShareConfigForPnk(@Validated @RequestBody APIRequest<SnapshotShareConfigReq> request) throws Exception{
        return iUser.selectAllSnapShareConfigForPnk(request);
    }

    @Override
    @SentinelResource(value = "/user/countRealUserAgentNumber")
    public APIResponse<Long> countRealUserAgentNumber(@Validated@RequestBody APIRequest<CountAgentNumberRequest> request) throws Exception {
        return iUser.countRealUserAgentNumber(request);
    }

    @Override
    public APIResponse<Boolean> signLVTRiskAgreement(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUserLVT.signLVTRiskAgreement(request);
    }

    @Override
    public APIResponse<RegisterUserResponse> thirdPartyUserRegister(@Validated @RequestBody APIRequest<ThirdPartyUserRegisterRequest> request) throws Exception {
        return iUser.thirdPartyUserRegister(request);
    }

    @Override
    public APIResponse<AccountForgotPasswordResponse> thirdPartySendResetPasswordEmail(@Validated @RequestBody APIRequest<AccountForgotPasswordRequest> request) throws Exception {
        return iUser.thirdPartySendResetPasswordEmail(request);
    }

    @Override
    public APIResponse<SignLVTStatusResponse> signLVTStatus(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUserLVT.signLVTStatus(request);
    }

    @Override
    public APIResponse<OneButtonRegisterResponse> oneButtonRegister(@Validated @RequestBody APIRequest<OneButtonRegisterRequest> request) throws Exception {
        return this.iUser.oneButtonRegister(request);
    }

    @Override
    @ServerRateLimiter(
            strategy = UserIdAndIPRateLimiterStrategy.class,
            useRedis = false,
            limitForPeriod = "${UserApi.oneButtonUserAccountActive.ratelimit.limitForPeriod:1}",
            limitRefreshPeriod = "${UserApi.oneButtonUserAccountActive.ratelimit.limitRefreshPeriod:1}"
    )
    public APIResponse<AccountActiveUserV2Response> oneButtonUserAccountActive(@Validated @RequestBody APIRequest<OneButtonUserAccountActiveRequest> request) throws Exception {
        return this.iUser.oneButtonUserAccountActive(request);
    }

    @Override
    @SentinelResource(value = "/user/financeFlag")
    public APIResponse<FinanceFlagResponse> financeFlag(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return this.iUser.financeFlag(request);
    }

    @SentinelResource(value = "/user/selectMiningUserAgentLog")
    public APIResponse<SearchResult<SelectUserAgentLogResponse>> selectMiningUserAgentLog(@RequestBody @Validated APIRequest<SelectMiningAgentLogRequest> request) throws Exception{
        return iUser.selectMiningUserAgentLog(request);
    }

    @Override
    @SentinelResource(value = "/user/selectMiningUserAgentNum")
    public APIResponse<Long> selectMiningUserAgentNum(@RequestBody @Validated APIRequest<SelectUserAgentNumRequest> request) throws Exception{
        return iUser.selectMiningUserAgentNum(request);
    }

    @Override
    @SentinelResource(value = "/user/createMiningAgentRate")
    public APIResponse<String> createMiningAgentRate(@RequestBody @Validated APIRequest<Long> request) throws Exception {
        return iUser.createMiningAgentRate(request);
    }


    @Override
    public APIResponse<UserSAQResponse> queryLVTSurveyStatus(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUser.queryLVTSAQStatus(request);
    }

    @Override
    public APIResponse<Boolean> finishLVTSAQ(@Validated @RequestBody APIRequest<UserIdReq> request) throws Exception {
        return iUser.finishLVTSAQ(request);
    }
  
    @Override
    public APIResponse<GetUserResponse> checkAndGetUserById(@Validated @RequestBody APIRequest<com.binance.account.vo.user.request.UserIdRequest> request) throws Exception {
        return this.iUser.checkAndGetUserById(request);
    }

    @Override
    public APIResponse<List<UserVo>> getUserStatusByUserIds(@Validated @RequestBody APIRequest<GetUserListRequest> request) throws Exception {
        return this.iUser.getUserStatusByUserIds(request);
    }
    
}
