package com.binance.account.api;

import com.binance.account.vo.user.MarketMakerUserVo;
import com.binance.account.vo.user.request.AddMarketMakerUserRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.MarketMakerUserRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author zhao chenkai
 * @date 2019/11/05
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/marketMaker/admin")
@Api(value = "做市商后台接口")
public interface MarketMakerAdminApi {

    @ApiOperation(notes = "添加做市商", nickname = "add", value = "添加做市商")
    @PostMapping("/add")
    APIResponse<Long> add(@RequestBody APIRequest<AddMarketMakerUserRequest> request) throws Exception;

    @ApiOperation(notes = "删除做市商", nickname = "delete", value = "删除做市商")
    @PostMapping("/delete")
    APIResponse<Void> delete(@RequestBody APIRequest<IdRequest> request) throws Exception;

    @ApiOperation(notes = "查询做市商账号", nickname = "marketMakerUserList", value = "查询做市商账号")
    @PostMapping("/marketMakerUserList")
    APIResponse<List<MarketMakerUserVo>> marketMakerUserList(@RequestBody APIRequest<MarketMakerUserRequest> request);

}
