package com.binance.account.controller.user;

import com.binance.account.vo.user.request.GenMarketMakerTransferTranIdRequest;
import com.binance.account.vo.user.request.MarketMakerAssetQuery;
import com.binance.account.vo.user.request.MarketMakerTransferRequest;
import com.binance.account.vo.user.response.GenMarketMakerTransferTranIdResponse;
import lombok.extern.log4j.Log4j2;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.MarketMakerApi;
import com.binance.account.service.user.IMarketMakerUser;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.math.BigDecimal;

/**
 * @author zhao chenkai
 * @date 2019/11/06
 */
@RestController
@Monitored
@Log4j2
public class MarketMakerController implements MarketMakerApi {

    @Autowired
    private IMarketMakerUser marketMakerUser;

    @Override
    public APIResponse<Boolean> isMarketMaker(@RequestBody APIRequest<IdRequest> request) throws Exception {
        return marketMakerUser.isMarketMaker(request);
    }

    @Override
    public APIResponse<GenMarketMakerTransferTranIdResponse> genMarketMakerTransferTranId(@RequestBody @Validated APIRequest<GenMarketMakerTransferTranIdRequest> request) throws Exception {
        return marketMakerUser.genMarketMakerTransferTranId(request);
    }

    @Override
    public APIResponse<Void> transferToMarketMaker(@RequestBody @Validated APIRequest<MarketMakerTransferRequest> request) throws Exception {
        return marketMakerUser.transferToMarketMaker(request);
    }

    @Override
    public APIResponse<Void> transferFromMarketMaker(@RequestBody @Validated APIRequest<MarketMakerTransferRequest> request) throws Exception {
        return marketMakerUser.transferFromMarketMaker(request);
    }

    @Override
    public APIResponse<BigDecimal> assetQuery(@RequestBody @Validated APIRequest<MarketMakerAssetQuery> request) throws Exception {
        return marketMakerUser.assetQuery(request);
    }

}
