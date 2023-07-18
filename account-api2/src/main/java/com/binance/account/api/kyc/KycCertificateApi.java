package com.binance.account.api.kyc;

import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.common.query.KycRefByNumberQuery;
import com.binance.account.common.query.KycRefQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.certificate.response.KycRefQueryByNumberResponse;
import com.binance.account.vo.certificate.response.KycRefQueryResponse;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoHistoryVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.*;
import com.binance.account.vo.kyc.response.*;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/kyc/certificate")
@Api(value = "KYC")
public interface KycCertificateApi {

    @ApiOperation("kyc认证当前状态")
    @PostMapping("/getKycStatus")
    APIResponse<GetKycStatusResponse> getKycStatus(@Validated @RequestBody APIRequest<Long> request);

    @ApiOperation("kyc认证基础信息提交")
    @PostMapping("/baseInfoSubmit")
    APIResponse<BaseInfoResponse> baseInfoSubmit(@Validated @RequestBody APIRequest<BaseInfoRequest> request);

    @ApiOperation("kyc认证基础信息IDM结果通知")
    @PostMapping("/idmAuthResult")
    APIResponse<Void> idmAuthResult(@Validated @RequestBody APIRequest<IdmAuthRequest> request);

    @ApiOperation("获取kyc认证的基础信息")
    @PostMapping("/getBaseInfo")
    APIResponse<BaseInfoResponse> getKycBaseInfo(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request);

    @ApiOperation("kyc认证地址认证信息提交")
    @PostMapping("/addressInfoSubmit")
    APIResponse<AddressInfoSubmitResponse> addressInfoSubmit(@Validated @RequestBody APIRequest<AddressInfoSubmitRequest> request);

    @ApiOperation("kyc认证地址认证审核提交")
    @PostMapping("/addressAuthResult")
    APIResponse<Void> addressAuthResult(@Validated @RequestBody APIRequest<AddresAuthResultRequest> request);

    @ApiOperation("kyc认证绑定kyc手机")
    @PostMapping("/kycBindMobile")
    APIResponse<Void> kycBindMobile(@Validated @RequestBody APIRequest<KycBindMobileRequest> request);

    @ApiOperation("kyc认证发送mobile code")
    @PostMapping("/kycSendSmsCode")
    APIResponse<Void> kycSendSmsCode(@Validated @RequestBody APIRequest<KycBindMobileRequest> request);

    @ApiOperation("kyc认证初始化JUMIO认证")
    @PostMapping("/kycJumioInit")
    APIResponse<JumioInitResponse> kycJumioInit(@Validated @RequestBody APIRequest<KycFlowRequest> request);

    @ApiOperation("kyc认证初始化Face认证")
    @PostMapping("/kycFaceInit")
    APIResponse<KycFaceInitResponse> kycFaceInit(@Validated @RequestBody APIRequest<FaceInitFlowRequest> request);

    @ApiOperation("kyc face ocr 认证")
    @PostMapping("/faceOcrSubmit")
    APIResponse<FaceOcrSubmitResponse> kycFaceOcrSubmit(@Validated @RequestBody APIRequest<FaceOcrSubmitRequest> request);


    // ---- admin ---
    @ApiOperation("kyc certificate list")
    @PostMapping("/getKycList")
    APIResponse<SearchResult<KycCertificateVo>> getKycCertificateList(@Validated @RequestBody APIRequest<KycCertificateQuery> request);

    @ApiOperation("kyc certificate detail")
    @PostMapping("/getKycDetail")
    APIResponse<KycCertificateVo> getKycCertificateDetail(@Validated @RequestBody APIRequest<Long> request);

    @ApiOperation("get kyc fill info")
    @PostMapping("/getKycFillInfo")
    APIResponse<KycFillInfoVo> getKycFillInfo(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request);

    @ApiOperation("get kyc fill histories")
    @PostMapping("/getKycFillInfoHistories")
    APIResponse<List<KycFillInfoHistoryVo>> getKycFillInfoHistories(@Validated @RequestBody APIRequest<GetBaseInfoRequest> request);

    @ApiOperation("get kyc certificate full detail")
    @PostMapping("/getKycCertificateFullDetail")
    APIResponse<KycCertificateVo> getKycCertificateFullDetail(@Validated @RequestBody APIRequest<Long> request);

    @ApiOperation("同步fiat pt 状态")
    @PostMapping("/syncFiatPtStatus")
    APIResponse<Boolean> syncFiatPtStatus(@Validated @RequestBody APIRequest<FiatKycSyncStatusRequest> request);

    @ApiOperation("kyc关联信息查询")
    @PostMapping("/kycRefQuery")
    APIResponse<SearchResult<KycRefQueryResponse>> kycRefQuery(@Validated @RequestBody APIRequest<KycRefQuery> request);

    @ApiOperation("身份证号关联查询kyc信息")
    @PostMapping("/kycRefQueryByNumber")
    APIResponse<SearchResult<KycRefQueryByNumberResponse>> kycRefQueryByNumber(@Validated @RequestBody APIRequest<KycRefByNumberQuery> request);


    @ApiOperation("删除用户kyc的证件信息")
    @PostMapping("/deleteKycNumberInfo")
    APIResponse<DeleteKycNumberInfoResponse> deleteKycNumberInfo(@Validated @RequestBody APIRequest<DeleteKycNumberInfoRequest> request);

    @ApiModelProperty("第三方补充信息")
    @PostMapping("/additional/info")
    APIResponse<KycFillInfoVo> additionalInfo(@Validated @RequestBody APIRequest<AdditionalInfoRequest> request);

    @ApiModelProperty("kyc change account change(certification-center用)")
    @PostMapping("/change/account/statusAndLevel")
    APIResponse<Void> changeAccountStatusAndLevel(@Validated @RequestBody APIRequest<KycAccountChangeRequest> request);

    @ApiModelProperty("kyc pass withdraw face change")
    @PostMapping("/change/withdrawFace/byKycPass")
    APIResponse<Void> changeWithdrawFaceByKycPass(@Validated @RequestBody APIRequest<KycPassWithdrawFaceRequest> request);
}
