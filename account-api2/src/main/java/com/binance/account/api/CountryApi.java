package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.country.*;
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

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/country")
@Api(value = "Country")
public interface CountryApi {

    @ApiOperation("移除缓存")
    @PostMapping("/removeCache")
    APIResponse<Boolean> removeCache(@RequestBody() APIRequest<String> request);

    @ApiOperation("判断国家是否在黑名单中（根据国家code）")
    @GetMapping("/blacklist/check/code")
    APIResponse<Boolean> isInBlacklist(@RequestParam("countryCode") String countryCode);

    @ApiOperation("判断用户是否在黑名单中（根据国家userId）")
    @GetMapping("/blacklist/check/userId")
    APIResponse<Boolean> isUserInBlacklist(@RequestParam("userId") Long userId);

    @ApiOperation("判断国家是否在黑名单中（根据IP）")
    @GetMapping("/blacklist/check/ip")
    APIResponse<Boolean> isIpInBlacklist(@RequestParam("ip") String ip);

    @ApiOperation("判断国家，城市或者地区是否在禁用表中（根据IP）")
    @GetMapping("/forbidden/check/ip")
    APIResponse<IpForbiddenCheckResponse> isIpInForbiddenRegion(@RequestParam("ip") String ip);

    @ApiOperation("查询国家黑名单列表")
    @GetMapping("/blacklist/list")
    APIResponse<List<CountryBlacklistVo>> queryBlacklist();

    @ApiOperation("新增黑名单国家")
    @PostMapping("/blacklist/add")
    APIResponse<Void> addBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request);

    @ApiOperation("更新黑名单国家")
    @PostMapping("/blacklist/update")
    APIResponse<Void> updateBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request);

    @ApiOperation("删除黑名单国家")
    @PostMapping("/blacklist/delete")
    APIResponse<Void> deleteBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request);

    @ApiOperation("根据国家三位字母代码（ISO 3166-1 alpha-3）查询国家信息")
    @GetMapping("/code/alpha3")
    APIResponse<CountryVo> getCountryByAlpha3(@RequestParam("alpha3") String alpha3);

    @ApiOperation("查询用户白名单")
    @PostMapping("/user/whitelist")
    APIResponse<SearchResult> queryWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistQuery> query);

    @ApiOperation("新增用户白名单")
    @PostMapping("/user/whitelist/add")
    APIResponse<Void> addWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistRequest> request);

    @ApiOperation("删除用户白名单")
    @PostMapping("/user/whitelist/delete")
    APIResponse<Void> deleteWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistRequest> request);

    @ApiOperation("获取国家列表，不包含黑名单国家")
    @PostMapping("/getCountryList")
    APIResponse<List<CountryVo>> getCountryList();

    @ApiOperation("获取全部国家列表")
    @GetMapping("/getAllCountryList")
    APIResponse<List<CountryVo>> getAllCountryList();

    @ApiOperation("根据国家代号获取国家列表")
    @PostMapping("/getCountryByCode")
    APIResponse<CountryVo> getCountryByCode(@RequestBody @Validated APIRequest<GetCountryByCodeRequest> request);

    @ApiOperation("根据mobileCode获取国家列表")
    @PostMapping("/getCountryByMobileCode")
    APIResponse<CountryVo> getCountryByMobileCode(@RequestBody @Validated APIRequest<String> request);

    @ApiOperation("判断国家是否在受限地区中（根据国家code）")
    @GetMapping("/restricted/by-ip")
    APIResponse<RestrictedCountryVo> isIpInRestrictedCountry(@RequestParam("ip") String ip);


    @ApiOperation("获取支持手机注册的国家信息")
    @PostMapping("/getMobileRegisterSupportCountryList")
    APIResponse<GetMobileRegisterSupportCountryListeResp> getMobileRegisterSupportCountryList(@RequestBody @Validated APIRequest<GetMobileRegisterSupportCountryListeRequest> request);

}
