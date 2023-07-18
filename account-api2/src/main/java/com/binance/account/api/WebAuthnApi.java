package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.WebAuthnAdminQuery;
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
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;

/**
 * @author liliang1 on 2019-05-21
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/webAuthn")
@Api(value = "Web Authn-Yubikey 验证")
public interface WebAuthnApi {


    @ApiOperation("开始绑定WebAuthn")
    @PostMapping("/startRegister")
    APIResponse<StartRegisterReponse> startRegister(@Validated @RequestBody APIRequest<StartRegisterRequest> request);

    @ApiOperation("结束绑定WebAuthn")
    @PostMapping("/finishRegister")
    APIResponse<Void> finishRegister(@Validated @RequestBody APIRequest<FinishRegisterRequest> request) throws Exception;

    @ApiOperation("结束绑定WebAuthn(等于把原来的finishRegister和activateRegister合并)")
    @PostMapping("/finishRegisterV2")
    APIResponse<Long> finishRegisterV2(@Validated @RequestBody APIRequest<FinishRegisterRequestV2> request) throws Exception;

    @ApiOperation("邮件激活绑定WebAuthn")
    @PostMapping("/activateRegister")
    APIResponse<Long> activateRegister(@Validated @RequestBody APIRequest<ActivateYubiKeyRequest> request);

    @ApiOperation("开始验证WebAuthn")
    @PostMapping("/startAuthenticate")
    APIResponse<StartAuthenticateResponse> startAuthenticate(@Validated @RequestBody APIRequest<StartAuthenticateRequest> request);


    @ApiOperation("结束验证WebAuthn")
    @PostMapping("/finishAuthenticate")
    APIResponse<Void> finishAuthenticate(@Validated @RequestBody APIRequest<FinishAuthenticateRequest> request);

    @ApiOperation("解绑WebAuthn")
    @PostMapping("/deregister")
    APIResponse<Void> deregister(@Validated @RequestBody APIRequest<DeregisterRequest> request);

    @ApiOperation("解绑WebAuthn")
    @PostMapping("/deregisterV2")
    APIResponse<Void> deregisterV2(@Validated @RequestBody APIRequest<DeregisterV2Request> request) throws Exception;

    @ApiOperation("解绑WebAuthn")
    @PostMapping("/deregister/v3")
    APIResponse<Void> deregisterV3(@Validated @RequestBody APIRequest<DeregisterV3Request> request) throws Exception;

    @ApiOperation("rename")
    @PostMapping("/rename")
    APIResponse<Boolean> rename(@Validated @RequestBody APIRequest<RenameYubikeyRequest> request);




    @ApiOperation("管理员后台解绑用户WebAuthn")
    @PostMapping("/deregister/admin")
    APIResponse<Integer> deregisterForAdmin(@Validated @RequestBody APIRequest<DeregisterForAdminRequest> request);

    @ApiOperation("获取某一次验证的结果")
    @PostMapping("/getAuthenticateResult")
    APIResponse<Boolean> getAuthenticateResult(@Validated @RequestBody APIRequest<AuthenticateResultRequest> request);

    @ApiOperation("获取用户绑定信息")
    @PostMapping("/getList")
    APIResponse<List<UserYubikeyVo>> getList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request);

    @ApiOperation("批量获取用户是否绑定")
    @PostMapping("/registered-user/batch")
    APIResponse<Set<Long>> batchUserRegistered(@Validated @RequestBody APIRequest<UserIdsRequest> request);

    @ApiOperation("域名是否支持绑定yubikey")
    @PostMapping("/origin-supported")
    APIResponse<Boolean> isOriginSupported(@Validated @RequestBody APIRequest<WebAuthnOriginSupportedRequest> request);

    // ----------注意： 以下是提供给管理后台的管理员用户做yubikey绑定验证的接口，不是提供给前台用户的 ----------//

    @ApiOperation("amdin-开始绑定WebAuthn")
    @PostMapping("/admin/startRegister")
    APIResponse<StartRegisterReponse> adminStartRegister(@Validated @RequestBody APIRequest<StartRegisterRequest> request);

    @ApiOperation("admin-结束绑定WebAuthn")
    @PostMapping("/admin/finishRegister")
    APIResponse<Boolean> adminFinishRegister(@Validated @RequestBody APIRequest<FinishRegisterRequest> request);

    @ApiOperation("开始验证WebAuthn")
    @PostMapping("/admin/startAuthenticate")
    APIResponse<StartAuthenticateResponse> adminStartAuthenticate(@Validated @RequestBody APIRequest<StartAuthenticateRequest> request);


    @ApiOperation("结束验证WebAuthn")
    @PostMapping("/admin/finishAuthenticate")
    APIResponse<Boolean> adminFinishAuthenticate(@Validated @RequestBody APIRequest<FinishAuthenticateRequest> request);

    @ApiOperation("/获取用户绑定信息")
    @PostMapping("/admin/getList")
    APIResponse<List<UserYubikeyVo>> adminGetList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request);

    @Deprecated
    @ApiOperation("/获取所有用户绑定信息")
    @PostMapping("/admin/getAllList")
    APIResponse<List<UserYubikeyVo>> adminGetAllList(@Validated @RequestBody APIRequest<WebAuthnListRequest> request);

    @ApiOperation("分页获取admin绑定信息")
    @PostMapping("/admin/getPageList")
    APIResponse<SearchResult<UserYubikeyVo>> adminGetPageList(@Validated @RequestBody APIRequest<WebAuthnAdminQuery> request);

    @ApiOperation("/强制解绑后台用户信息")
    @PostMapping("/admin/deregister-force")
    APIResponse<Void> adminDeregister(@Validated @RequestBody APIRequest<WebAuthnListRequest> request);

}
