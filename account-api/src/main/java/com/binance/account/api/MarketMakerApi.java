package com.binance.account.api;

import java.math.BigDecimal;

import com.binance.account.vo.user.response.GenMarketMakerTransferTranIdResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.user.request.GenMarketMakerTransferTranIdRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.MarketMakerAssetQuery;
import com.binance.account.vo.user.request.MarketMakerTransferRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author zhao chenkai
 * @date 2019/11/26
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/marketMaker")
@Api(value = "做市商接口")
public interface MarketMakerApi {

    @ApiOperation(notes = "查询某账号是否是做市商账号", nickname = "isMarketMaker", value = "查询某账号是否是做市商账号")
    @PostMapping("/isMarketMaker")
    APIResponse<Boolean> isMarketMaker(@RequestBody APIRequest<IdRequest> request) throws Exception;

    @ApiOperation(notes = "生成做市商划转tranId", nickname = "genMarketMakerTransferTranId", value = "生成做市商划转tranId")
    @PostMapping("/genMarketMakerTransferTranId")
    APIResponse<GenMarketMakerTransferTranIdResponse> genMarketMakerTransferTranId(@RequestBody APIRequest<GenMarketMakerTransferTranIdRequest> request) throws Exception;

    @ApiOperation(notes = "从对公账号划转给做市商账号", nickname = "transferToMarketMaker", value = "从对公账号划转给做市商账号")
    @PostMapping("/transferToMarketMaker")
    APIResponse<Void> transferToMarketMaker(@RequestBody APIRequest<MarketMakerTransferRequest> request) throws Exception;

    @ApiOperation(notes = "从做市商账号划转给对公账号", nickname = "transferFromMarketMaker", value = "从做市商账号划转给对公账号")
    @PostMapping("/transferFromMarketMaker")
    APIResponse<Void> transferFromMarketMaker(@RequestBody APIRequest<MarketMakerTransferRequest> request) throws Exception;
    
    @ApiOperation(notes = "查询做市商对公账号资产余额", nickname = "assetQuery", value = "查询做市商对公账号资产余额")
    @PostMapping("/assetQuery")
    APIResponse<BigDecimal> assetQuery(@RequestBody APIRequest<MarketMakerAssetQuery> request) throws Exception;

}
