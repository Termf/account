package com.binance.account.controller.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.api.WebAuthnApi;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.WebAuthnAdminQuery;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.request.UserIdsRequest;
import com.binance.account.vo.yubikey.ActivateYubiKeyRequest;
import com.binance.account.vo.yubikey.AuthenticateResultRequest;
import com.binance.account.vo.yubikey.DeregisterForAdminRequest;
import com.binance.account.vo.yubikey.DeregisterRequest;
import com.binance.account.vo.yubikey.DeregisterV2Request;
import com.binance.account.vo.yubikey.DeregisterV3Request;
import com.binance.account.vo.yubikey.FinishAuthenticateRequest;
import com.binance.account.vo.yubikey.FinishRegisterRequest;
import com.binance.account.vo.yubikey.FinishRegisterRequestV2;
import com.binance.account.vo.yubikey.RenameYubikeyRequest;
import com.binance.account.vo.yubikey.StartAuthenticateRequest;
import com.binance.account.vo.yubikey.StartAuthenticateResponse;
import com.binance.account.vo.yubikey.StartRegisterReponse;
import com.binance.account.vo.yubikey.StartRegisterRequest;
import com.binance.account.vo.yubikey.UserYubikeyVo;
import com.binance.account.vo.yubikey.WebAuthnListRequest;
import com.binance.account.vo.yubikey.WebAuthnOriginSupportedRequest;
import com.binance.account.yubikey.WebAuthnAdminHandler;
import com.binance.account.yubikey.WebAuthnFrontHandler;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class WebAuthnController implements WebAuthnApi {

    @Autowired
    private WebAuthnFrontHandler webAuthnFrontHandler;

    @Autowired
    private WebAuthnAdminHandler webAuthnAdminHandler;

    @Autowired
    private IUserSecurity iUserSecurity;

    @Override
    public APIResponse<StartRegisterReponse> startRegister(@Validated @RequestBody APIRequest<StartRegisterRequest> request) {
        StartRegisterRequest registerRequest = request.getBody();
        Long userId = registerRequest.getUserId();
        String origin  = registerRequest.getOrigin();
        String nickname = registerRequest.getNickname();
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.startRegistration(userId, origin, nickname));
    }

    @Override
    public APIResponse<Void> finishRegister(@Validated @RequestBody APIRequest<FinishRegisterRequest> request) throws Exception  {
        boolean success = webAuthnFrontHandler.finishRegistration(request.getBody());
        if (success) {
            return APIResponse.getOKJsonResult();
        } else {
            return APIResponse.getErrorJsonResult("Registration Failed");
        }
    }

    @Override
    public APIResponse<Long> finishRegisterV2(@Validated @RequestBody APIRequest<FinishRegisterRequestV2> request) throws Exception {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.finishRegisterV2(request.getBody()));
    }

    @Override
    public APIResponse<Long> activateRegister(@Validated @RequestBody APIRequest<ActivateYubiKeyRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.activate(request.getBody()));
    }

    @Override
    public APIResponse<StartAuthenticateResponse> startAuthenticate(@Validated @RequestBody APIRequest<StartAuthenticateRequest> request) {
        StartAuthenticateRequest authenticateRequest = request.getBody();
        Long userId = authenticateRequest.getUserId();
        String origin  = authenticateRequest.getOrigin();
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.startAuthenticate(userId, origin));
    }

    @Override
    public APIResponse<Void> finishAuthenticate(@Validated @RequestBody APIRequest<FinishAuthenticateRequest> request) {
        FinishAuthenticateRequest finishAuthenticateRequest = request.getBody();
        Long userId = finishAuthenticateRequest.getUserId();
        String detail = finishAuthenticateRequest.getFinishDetail();
        boolean success = webAuthnFrontHandler.finishAuthenticate(userId, detail, false);
        if (success) {
            return APIResponse.getOKJsonResult();
        } else {
            return APIResponse.getErrorJsonResult("Authenticate failed");
        }

    }

    @Override
    public APIResponse<Void> deregister(@Validated @RequestBody APIRequest<DeregisterRequest> request) {
        DeregisterRequest deregisterRequest = request.getBody();
        try {
            webAuthnFrontHandler.check2Fa(deregisterRequest.getUserId(), deregisterRequest.getAuthType(), deregisterRequest.getCode());
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            } else {
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
        }
        JSONObject finishDetail;
        try {
            finishDetail = JSON.parseObject(deregisterRequest.getFinishDetail());
        } catch (Exception e) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "finishDetail");
        }
        finishDetail.put("deregister", true);
        boolean success = webAuthnFrontHandler.finishAuthenticate(deregisterRequest.getUserId(), JSON.toJSONString(finishDetail), true);
        if (success) {
            return APIResponse.getOKJsonResult();
        } else {
            return APIResponse.getErrorJsonResult("deregister failed.");
        }
    }

    @Override
    public APIResponse<Void> deregisterV2(@Validated @RequestBody APIRequest<DeregisterV2Request> request) throws Exception {
        DeregisterV2Request requestBody = request.getBody();

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.DEREGISTER_YUBIKEY)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        iUserSecurity.verifyMultiFactors(verify);

        JSONObject finishDetail;
        try {
            finishDetail = JSON.parseObject(requestBody.getFinishDetail());
        } catch (Exception e) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "finishDetail");
        }
        finishDetail.put("deregister", true);
        boolean success = webAuthnFrontHandler.finishAuthenticate(requestBody.getUserId(), JSON.toJSONString(finishDetail), true);
        if (success) {
            return APIResponse.getOKJsonResult();
        } else {
            return APIResponse.getErrorJsonResult("deregister failed.");
        }
    }

    @Override
    public APIResponse<Void> deregisterV3(APIRequest<DeregisterV3Request> request) throws Exception {
        return webAuthnFrontHandler.deregisterV3(request.getBody());
    }

    @Override
    public APIResponse<Integer> deregisterForAdmin(@Validated @RequestBody APIRequest<DeregisterForAdminRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.deregisterForAdmin(request.getBody().getUserId()));
    }

    @Override
    public APIResponse<Boolean> getAuthenticateResult(@Validated @RequestBody APIRequest<AuthenticateResultRequest> request) {
        AuthenticateResultRequest resultRequest = request.getBody();
        Long userId = resultRequest.getUserId();
        String requestId = resultRequest.getRequestId();
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.getAuthenticateResult(userId, requestId));
    }

    @Override
    public APIResponse<List<UserYubikeyVo>> getList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.getList(request.getBody()));
    }

    @Override
    public APIResponse<Set<Long>> batchUserRegistered(@Validated @RequestBody APIRequest<UserIdsRequest> request) {
        return APIResponse.getOKJsonResult(Sets.newHashSet(webAuthnFrontHandler.registerdYubikeyUserIds(request.getBody().getUserIds())));
    }

    @Override
    public APIResponse<Boolean> isOriginSupported(@Validated @RequestBody APIRequest<WebAuthnOriginSupportedRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.isOriginSupported(request.getBody().getOrigin()));
    }

    @Override
    public APIResponse<Boolean> rename(APIRequest<RenameYubikeyRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnFrontHandler.renameYubikey(request.getBody()));
    }

    // 以下是提供给管理后台的管理员用户做yubikey绑定验证的接口，不是提供给前台用户的

    @Override
    public APIResponse<StartRegisterReponse> adminStartRegister(@Validated @RequestBody APIRequest<StartRegisterRequest> request) {
        StartRegisterRequest registerRequest = request.getBody();
        Long userId = registerRequest.getUserId();
        String origin  = registerRequest.getOrigin();
        String nickname = registerRequest.getNickname();
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.startRegistration(userId, origin, nickname));
    }

    @Override
    public APIResponse<Boolean> adminFinishRegister(@Validated @RequestBody APIRequest<FinishRegisterRequest> request) {
        FinishRegisterRequest finishRegisterRequest = request.getBody();
        Long userId = finishRegisterRequest.getUserId();
        String detail = finishRegisterRequest.getFinishDetail();
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.finishRegistration(userId, detail));
    }

    @Override
    public APIResponse<StartAuthenticateResponse> adminStartAuthenticate(@Validated @RequestBody APIRequest<StartAuthenticateRequest> request) {
        StartAuthenticateRequest authenticateRequest = request.getBody();
        Long userId = authenticateRequest.getUserId();
        String origin  = authenticateRequest.getOrigin();
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.startAuthenticate(userId, origin));
    }

    @Override
    public APIResponse<Boolean> adminFinishAuthenticate(@Validated @RequestBody APIRequest<FinishAuthenticateRequest> request) {
        FinishAuthenticateRequest finishAuthenticateRequest = request.getBody();
        Long userId = finishAuthenticateRequest.getUserId();
        String detail = finishAuthenticateRequest.getFinishDetail();
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.finishAuthenticate(userId, detail, true));
    }

    @Override
    public APIResponse<List<UserYubikeyVo>> adminGetList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.getList(request.getBody().getUserId(), request.getBody().getOrigin()));
    }

    @Override
    public APIResponse<List<UserYubikeyVo>> adminGetAllList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request) {
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.getAllList());
    }

    @Override
    public APIResponse<SearchResult<UserYubikeyVo>> adminGetPageList(@Validated @RequestBody APIRequest<WebAuthnAdminQuery> request) {
        return APIResponse.getOKJsonResult(webAuthnAdminHandler.adminGetPageList(request.getBody()));
    }

    @Override
    public APIResponse<Void> adminDeregister(@Validated @RequestBody APIRequest<WebAuthnListRequest> request) {
        webAuthnAdminHandler.deregisterForce(request.getBody().getUserId(), request.getBody().getOrigin());
        return APIResponse.getOKJsonResult();
    }
}
