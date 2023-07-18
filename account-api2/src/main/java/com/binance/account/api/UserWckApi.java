package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.UserWckAuditVo;
import com.binance.account.vo.certificate.request.UserChannelWckQuery;
import com.binance.account.vo.certificate.request.UserWckQuery;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import com.binance.account.vo.certificate.request.WckChannelAuditRequest;
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

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/user/kyc/wck")
@Api(value = "用户KYC: World-Check One相关接口")
public interface UserWckApi {

    @ApiOperation("获取World Check审核列表")
    @PostMapping("/audit/list")
    APIResponse<SearchResult<UserWckAuditVo>> getList(@RequestBody() APIRequest<UserWckQuery> request);

    @ApiOperation("分页获取channel World Check审核列表")
    @PostMapping("/audit/listByPages")
    APIResponse<SearchResult<UserChannelWckAuditVo>> getListByPages(@RequestBody() APIRequest<UserChannelWckQuery> request);

    @ApiOperation("World Check人工审核提交")
    @PostMapping("/audit/commit")
    APIResponse<Void> audit(@RequestBody @Validated APIRequest<WckAuditRequest> request);

    @ApiOperation("获取world check信息")
    @GetMapping("/results")
    APIResponse<?> getWckResultProfile(@RequestParam("kycId") Long kycId);

    @ApiOperation("获取world check开关")
    @GetMapping("/switch")
    APIResponse<Boolean> isWckSwitch();

    @ApiOperation("新World Check人工审核提交")
    @PostMapping("/newAudit/commit")
    APIResponse<Void> newAudit(@RequestBody @Validated APIRequest<WckChannelAuditRequest> request);

    @ApiOperation("获取channel用户 world check信息")
    @GetMapping("/channelResults")
    APIResponse<?> getChannelWckResultProfile(@RequestParam("caseId") String caseId);

    @ApiOperation("重置channel用户 world check任务")
    @GetMapping("/reset")
    APIResponse<?> resetChannelWck(@RequestParam("caseId") String caseId);
}
