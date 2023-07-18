package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserAddressQuery;
import com.binance.account.vo.user.UserAddressVo;
import com.binance.account.vo.user.request.AddressAuditRequest;
import com.binance.account.vo.user.request.UserAddressChangeStatusRequest;
import com.binance.account.vo.user.request.UserAddressRequest;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE)
@RequestMapping(value = "/user/address")
@Api(value = "用户地址")
public interface UserAddressApi {

    @ApiOperation(notes = "获取地址审核列表", nickname = "getAddressList", value = "获取地址审核列表")
    @PostMapping("/getList")
    APIResponse<SearchResult<UserAddressVo>> getList(@RequestBody() APIRequest<UserAddressQuery> request) throws Exception;

    @ApiOperation(notes = "地址人工审核", nickname = "audit", value = "地址人工审核")
    @PostMapping("/audit")
    APIResponse<?> audit(@RequestBody() APIRequest<AddressAuditRequest> request) throws Exception;

    @ApiOperation(notes = "提交地址认证信息", nickname = "baseInfoSubmit", value = "提交地址认证信息")
    @PostMapping("/submit")
    APIResponse<?> submit(@RequestBody() APIRequest<UserAddressRequest> request);

    @ApiOperation(notes = "把最后一笔通过的地址验证设置为过期状态", nickname = "baseInfoSubmit", value = "把最后一笔通过的地址验证设置为过期状态")
    @PostMapping("/expired")
    APIResponse<Void> updatePassedToExpired(@RequestBody() APIRequest<UserAddressChangeStatusRequest> request);

}
