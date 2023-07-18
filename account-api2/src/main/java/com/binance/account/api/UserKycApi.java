package com.binance.account.api;

import com.binance.account.common.query.JumioBizStatusQuery;
import com.binance.account.common.query.UserKycModularQuery;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.response.KycFormAddrResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.certificate.response.UserSimpleBaseInfoResponse;
import com.binance.account.vo.security.request.ChainAddressAnalyzeRequest;
import com.binance.account.vo.security.request.ChainAddressAuditRequest;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.GetUserListResponse;
import com.binance.account.vo.user.response.InitSdkUserKycResponse;
import com.binance.master.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.JumioQuery;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/user/kyc")
@Api(value = "用户KYC")
public interface UserKycApi {

    @ApiOperation(notes = "获取KYC通过的用户国籍", nickname = "getKycCountry", value = "获取KYC通过的用户国籍")
    @PostMapping("/getKycCountry")
    APIResponse<UserKycCountryResponse> getKycCountry(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation(notes = "获取kyc通过用户", nickname = "getApproveUser", value = "获取kyc通过用户")
    @PostMapping("/getApproveUser")
    APIResponse<UserKycApproveVo> getApproveUser(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation(notes = "判断用户是否通过kyc", nickname = "checkUserWhetherPassKyc", value = "判断用户是否通过kyc")
    @PostMapping("/checkUserWhetherPassKyc")
    APIResponse<Boolean> checkUserWhetherPassKyc(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation(notes = "更新kyc通过用户", nickname = "updateKycApprove", value = "更新kyc通过用户")
    @PostMapping("/updateKycApprove")
    APIResponse<?> updateKycApprove(@RequestBody() APIRequest<UpdateKycApproveRequest> request) throws Exception;

    @ApiOperation(notes = "获取kyc通过列表", nickname = "getApproveList", value = "获取kyc通过列表")
    @PostMapping("/getApproveList")
    APIResponse<SearchResult<UserKycApproveVo>> getApproveList(@RequestBody() APIRequest<JumioQuery> request) throws Exception;

    @ApiOperation(notes = "提交个人基本信息", nickname = "submitBaseInfo", value = "提交个人基本信息")
    @PostMapping("/submitBaseInfo")
    APIResponse<JumioTokenResponse> submitBaseInfo(@RequestBody() APIRequest<KycBaseInfoRequest> request) throws Exception;

    @ApiOperation(notes = "SDK端提交个人KYC基本信息", nickname = "initSdkUserKyc", value = "SDK端提交个人KYC基本信息")
    @PostMapping("/initSdkUserKyc")
    APIResponse<InitSdkUserKycResponse> initSdkUserKyc(@RequestBody() @Validated APIRequest<KycBaseInfoRequest> request) throws Exception;

    @ApiOperation(notes = "jumio sdk scanRef同步", nickname = "saveJumioSdkScanRef", value = "jumio sdk scanRef同步")
    @PostMapping("/saveJumioSdkScanRef")
    APIResponse<Void> saveJumioSdkScanRef(@RequestBody @Validated APIRequest<SaveJumioSdkScanRefRequest> request) throws Exception;

    @ApiOperation(notes = "获取kyc审核列表", nickname = "getKycList", value = "获取kyc审核列表")
    @PostMapping("/getList")
    APIResponse<SearchResult<UserKycVo>> getList(@RequestBody() APIRequest<JumioQuery> request) throws Exception;

    @ApiOperation(notes = "kyc人工审核", nickname = "audit", value = "kyc人工审核")
    @PostMapping("/audit")
    APIResponse<?> audit(@RequestBody() APIRequest<KycAuditRequest> request) throws Exception;

    @ApiOperation(notes = "同步KYC照片", nickname = "audit", value = "同步KYC照片")
    @PostMapping("/syncPhoto")
    APIResponse<?> syncPhoto(@Validated @RequestBody() APIRequest<JumioQuery> request) throws Exception;
    
    @Deprecated
    @ApiOperation(notes = "获取kyc信息", nickname = "getKycByUserId", value = "获取kyc信息")
    @PostMapping("/getKycByUserId")
    APIResponse<UserKycVo> getKycByUserId(@Validated @RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation(notes = "获取出入金地址审核信息", nickname = "getChainAddressAuditPage", value = "获取出入金地址审核信息")
    @PostMapping("/getChainAddressAuditPage")
    APIResponse<?> getChainAddressAuditPage(@RequestBody() APIRequest<ChainAddressAuditRequest> request);

    @ApiOperation(notes = "提交出入金地址审核信息", nickname = "submitChainAudit", value = "前台提交出入金地址审核")
    @PostMapping("/submitChainAudit")
    APIResponse<?> submitChainAddressAudit(@Validated @RequestBody() APIRequest<ChainAddressAnalyzeRequest> request);

    @ApiOperation("判断地址是否在白名单中")
    @GetMapping("/isAddressInWhitelist")
    APIResponse<Boolean> isAddressInWhitelist(@RequestParam("address") String address);

    @ApiOperation(notes = "审核出入金地址", nickname = "auditChainAddress", value = "后台人工审核出入金地址")
    @PostMapping("/auditChainAddress")
    APIResponse<?> auditChainAddress(@Validated @RequestBody() APIRequest<ChainAddressAuditRequest> request);

    @ApiOperation(notes = "用户KYC模块化列表信息", nickname = "auditChainAddress", value = "后台用户KYC模块化列表信息")
    @PostMapping("/modular/getUserKycList")
    APIResponse<SearchResult<UserKycVo>> getModularUserKycList(@Validated @RequestBody() APIRequest<UserKycModularQuery> request);

    @ApiOperation("查询Jumio对应业务的业务状态")
    @PostMapping("/jumio/bizStatus")
    APIResponse<String> queryJumioBizStatus(@Validated @RequestBody APIRequest<JumioBizStatusQuery> request);
    
    @ApiOperation(notes = "获取kyc通过用户和id", nickname = "getApproveUserById", value = "获取kyc通过用户和id")
    @PostMapping("/getUserKycById")
    APIResponse<UserKycVo> getUserKycById(@Validated @RequestBody() APIRequest<UserIdAndIdRequest> request) throws Exception;

    /**
     *
     * @param request
     * @return
     */
    @ApiOperation(notes = "同步jumio审核信息", nickname = "syncJumioAuditResult", value = "同步jumio审核信息")
    @PostMapping("/syncJumioAuditResult")
    APIResponse<?> syncJumioAuditResult(@Validated @RequestBody APIRequest<String> request);

    /**
     * 获取当前KYC的认证状态
     * @param request
     * @return
     */
    @ApiOperation(notes = "KYC认证当前状态信息", nickname = "currentKycStatus", value = "KYC认证当前状态信息")
    @PostMapping("/currentKycStatus")
    APIResponse<KycDetailResponse> getCurrentKycStatus(@Validated @RequestBody() APIRequest<UserIdRequest> request);

    /**
     * 强制把已经通过的KYC状态变更到过期的状态
     * (2019-04-25 目前主要用于SGP交易所的需求，实际该接口可以通用，慎用)
     * @param request
     * @return
     */
    @ApiOperation("强制把已经通过的KYC重置到过期状态")
    @PostMapping("/force/expired/kyc")
    APIResponse<Void> forceKycPassedToExpired(@Validated @RequestBody() APIRequest<KycForceToExpiredRequest> request);

    /**
     * 该方法目前只针对 新加坡交易所，直接补充KYC数据
     * @param request
     * @return
     */
    @ApiOperation(notes = "保存XfersKYC数据", nickname = "saveXfersKycData", value = "保存XfersKYC数据")
    @PostMapping("/saveXferKycData")
    APIResponse<Boolean> saveXfersKycData(@Validated @RequestBody APIRequest<UserKycVo> request);
    
    
    /**
     * 该方法目前只针对 新加坡交易所，直接补充KYC数据
     * @param request
     * @return
     */
    @ApiOperation(notes = "更新XfersKYC数据", nickname = "updateXfersKycData", value = "更新XfersKYC数据")
    @PostMapping("/updateXfersKycData")
    APIResponse<Boolean> updateXfersKycData(@Validated @RequestBody APIRequest<UserKycVo> request);
    
    /**
     * Approve 已通过审核重置为失败
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation(notes = "kyc审核通过重置为失败", nickname = "refuseApprove", value = "kyc审核通过重置为失败")
    @PostMapping("/refuseApprove")
    APIResponse<?> refuseApprove(@RequestBody() APIRequest<KycAuditRequest> request) throws Exception;

    /**
     * 批量获取用户kyc认证，表单输入的地址
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation("批量获取用户Kyc表单地址")
    @PostMapping("/getKycFormAddrByUserIds")
    APIResponse<KycFormAddrResponse> getKycFormAddrByUserIds(@RequestBody APIRequest<GetUserListRequest> request) throws Exception;

    @ApiOperation(notes = "UG保存个人简单基本信息", nickname = "saveSimpleBaseInfo", value = "保存个人简单基本信息")
    @PostMapping("/submitSimpleBaseInfo")
    APIResponse<Boolean> submitSimpleBaseInfo(@RequestBody() APIRequest<KycSimpleBaseInfoRequest> request) throws Exception;

    @ApiOperation(notes = "UG获取个人简单基本信息", nickname = "getSimpleBaseInfo", value = "获取个人简单基本信息")
    @PostMapping("/getSimpleBaseInfo")
    APIResponse<UserSimpleBaseInfoResponse> getSimpleBaseInfo(@RequestBody() APIRequest<UserIdRequest> request) throws Exception;

}
