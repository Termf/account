package com.binance.account.api;

import com.binance.account.common.query.UserChannelRiskCountryQuery;
import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.vo.UserChannelRiskCountryVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingRuleVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.request.ChannelRiskRatingRuleAuditRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyResponse;
import com.binance.account.vo.certificate.request.RiskRatingChangeLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeStatusRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeTierLevelRequest;
import com.binance.account.vo.certificate.request.RiskRatingLimitResponse;
import com.binance.account.vo.certificate.request.RiskRatingStockUserImport;
import com.binance.account.vo.certificate.request.SyncRiskRatingCardCountryRequest;
import com.binance.account.vo.certificate.request.UserChannelRiskRatingRequest;
import com.binance.master.commons.SearchResult;
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

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户身份证")
@RequestMapping("/userChannelRiskRating")
public interface UserChannelRiskRatingApi  {

    @ApiOperation("查询用户渠道风控评分列表")
    @PostMapping("/getPageList")
    APIResponse<SearchResult<UserChannelRiskRatingVo>> getUserChannelRiskRatings(@Validated @RequestBody APIRequest<UserChannelRiskRatingQuery> request);

    @ApiOperation("变更状态")
    @PostMapping("/changeStatus")
    APIResponse<Void> changeUserRiskRatingStatus(@Validated @RequestBody APIRequest<RiskRatingChangeStatusRequest> request);

    @ApiOperation("变更每月限额")
    @PostMapping("/changeLimit")
    APIResponse<Void> changeRiskRatingLimit(@Validated @RequestBody APIRequest<RiskRatingChangeLimitRequest> request);

    @ApiOperation("变更TierLevel")
    @PostMapping("/changeTierLevel")
    APIResponse<Void> changeUserRiskRatingTierLevel(@Validated @RequestBody APIRequest<RiskRatingChangeTierLevelRequest> request);

    @ApiOperation("查询规则信息")
    @GetMapping("/getChannelRuleInfoByRatingId")
    APIResponse<List<UserChannelRiskRatingRuleVo>> getRiskRuleInfoByRiskRatingId(@RequestParam("userId") Long userId, @RequestParam("riskRatingId") Integer riskRatingId);

    @ApiOperation("重跑riskRating评分规则")
    @GetMapping("/redoRiskRatingRule")
    APIResponse<Void> redoRiskRatingRule(@RequestParam("userId") Long userId, @RequestParam("riskRatingId") Integer riskRatingId);

    @ApiOperation("修改riskRating评分规则")
    @PostMapping("/auditRiskRule")
    APIResponse<Void> auditRiskRule(@Validated @RequestBody APIRequest<ChannelRiskRatingRuleAuditRequest> request);

    @ApiOperation("查询规则信息")
    @PostMapping("/getChannelRiskCountryList")
    APIResponse<SearchResult<UserChannelRiskCountryVo>> getChannelRiskCountryList(@Validated @RequestBody APIRequest<UserChannelRiskCountryQuery> request);

    @ApiOperation("修改/添加渠道风险国籍信息")
    @PostMapping("/saveChannelRiskCountry")
    APIResponse<Integer> saveChannelRiskCountry(@Validated @RequestBody APIRequest<UserChannelRiskCountryVo> request);

    @ApiOperation("删除渠道风险国籍信息")
    @PostMapping("/deleteChannelRiskCountry")
    APIResponse<Integer> deleteChannelRiskCountry(@Validated @RequestBody APIRequest<UserChannelRiskCountryVo> request);
    
    @ApiOperation("获取riskRating限额")
    @PostMapping("/riskRatingLimit")
    APIResponse<RiskRatingLimitResponse> riskRatingLimit(@Validated @RequestBody APIRequest<UserChannelRiskRatingRequest> request);
    
    @ApiOperation("riskRating申报")
    @PostMapping("/riskRatingApply")
    APIResponse<RiskRatingApplyResponse> riskRatingApply(@Validated @RequestBody APIRequest<RiskRatingApplyRequest> request);
    
    @ApiOperation("同步卡归属国")
    @PostMapping("/syncCardCountry")
    APIResponse<Void> syncCardCountry(@Validated @RequestBody APIRequest<SyncRiskRatingCardCountryRequest> request);
    
    @ApiOperation("查询规则信息")
    @GetMapping("/getUserRiskRating")
    APIResponse<UserChannelRiskRatingVo> getUserRiskRating(@RequestParam("userId") Long userId, @RequestParam("riskRatingId") Integer riskRatingId);
    
    @ApiOperation("存量用户导入")
    @PostMapping("/batchImportRiskRating")
    APIResponse<Void> batchImportRiskRating(@Validated @RequestBody APIRequest<RiskRatingStockUserImport> request);
}
