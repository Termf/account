package com.binance.account.api.kyc;

import com.binance.account.common.query.TaxIdBlacklistQuery;
import com.binance.account.vo.certificate.TaxIdBlacklistVo;
import com.binance.account.vo.kyc.request.FaceAuthRequest;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.TaxIdBlacklistPushRequest;
import com.binance.account.vo.kyc.request.UpdateFaceOcrNameRequest;
import com.binance.account.vo.kyc.request.UpdateFillNameRequest;
import com.binance.master.commons.SearchResult;
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
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/kyc/certificate/audit")
@Api(value = "KYC-AUTH")
public interface KycCertificateAuditApi {
	
	@ApiOperation("kyc认证当前状态")
    @PostMapping("/baseInfo")
    APIResponse<Void> auditBaseInfo(@Validated @RequestBody APIRequest<KycAuditRequest> request);


	@ApiOperation("kyc认证当前状态")
    @PostMapping("/googleForm")
    APIResponse<Void> auditGoogleForm(@Validated @RequestBody APIRequest<KycAuditRequest> request);
	
	
	@ApiOperation("修改faceOcrName")
    @PostMapping("/updateFaceOcrName")
    APIResponse<Void> updateFaceOcrName(@Validated @RequestBody APIRequest<UpdateFaceOcrNameRequest> request);
	
	@ApiOperation("kyc认证审核FaceOcr")
    @PostMapping("/faceOcr")
    APIResponse<Void> auditFaceOcr(@Validated @RequestBody APIRequest<FaceOcrAuthRequest> request);
	
	@ApiOperation("kyc认证审核Face")
    @PostMapping("/face")
	APIResponse<Void> auditFace(@Validated @RequestBody APIRequest<FaceAuthRequest> request);

    @ApiOperation("修改用户填写的姓名")
    @PostMapping("/updateFillName")
    APIResponse<Void> updateFillName(@Validated @RequestBody APIRequest<UpdateFillNameRequest> request);

    @ApiOperation("查询TaxId黑名单")
    @PostMapping("/queryTaxIdBlacklist")
    APIResponse<SearchResult<TaxIdBlacklistVo>> queryTaxIdBlacklist(@RequestBody APIRequest<TaxIdBlacklistQuery> request);

    @ApiOperation("把TaxId加入黑名单")
    @PostMapping("/pushTaxIdToBlacklist")
    APIResponse<Void> pushTaxIdToBlacklist(@Validated @RequestBody APIRequest<TaxIdBlacklistPushRequest> request);

    @ApiOperation("把TaxId从黑名单移除")
    @PostMapping("/removeTaxIdFromBlacklist")
    APIResponse<Void> removeTaxIdFromBlacklist(@RequestParam("taxId") String taxId);
    
}
