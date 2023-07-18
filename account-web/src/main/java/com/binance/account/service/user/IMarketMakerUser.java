package com.binance.account.service.user;

import java.math.BigDecimal;
import java.util.List;

import com.binance.account.vo.user.MarketMakerUserVo;
import com.binance.account.vo.user.request.AddMarketMakerUserRequest;
import com.binance.account.vo.user.request.GenMarketMakerTransferTranIdRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.MarketMakerAssetQuery;
import com.binance.account.vo.user.request.MarketMakerTransferRequest;
import com.binance.account.vo.user.request.MarketMakerUserRequest;
import com.binance.account.vo.user.response.GenMarketMakerTransferTranIdResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * @author zhao chenkai
 * @date 2019/11/05
 */
public interface IMarketMakerUser {

    /**
     * 添加做市商
     * @param request
     * @return
     */
    APIResponse<Long> add(APIRequest<AddMarketMakerUserRequest> request);

    /**
     * 删除做市商
     * @param request
     * @return
     */
    APIResponse<Void> delete(APIRequest<IdRequest> request);

    /**
     * 判断某账号是否是做市商账号
     * @param request
     * @return
     */
    APIResponse<Boolean> isMarketMaker(APIRequest<IdRequest> request);

    /**
     * 做市商账号查询
     * @param request
     * @return
     */
    APIResponse<List<MarketMakerUserVo>> marketMakerUserList(APIRequest<MarketMakerUserRequest> request);

    /**
     * 做市商账号enable 2fa
     * @param userId
     */
    void marketMakerEnable2fa(Long userId);

    /**
     * 从对公账号划转给做市商账号
     * @param request
     * @return
     */
    APIResponse<Void> transferToMarketMaker(APIRequest<MarketMakerTransferRequest> request) throws Exception;

    /**
     * 查询做市商对公账号资产余额
     * @param request
     * @return
     */
    APIResponse<BigDecimal> assetQuery(APIRequest<MarketMakerAssetQuery> request) throws Exception;

    /**
     * 生成做市商划转tranId
     * @param request
     * @return
     */
    APIResponse<GenMarketMakerTransferTranIdResponse> genMarketMakerTransferTranId(APIRequest<GenMarketMakerTransferTranIdRequest> request) throws Exception;


    /**
     * 从做市商账号划转给对公账号
     * @param request
     * @return
     */
    APIResponse<Void> transferFromMarketMaker(APIRequest<MarketMakerTransferRequest> request) throws Exception;
    
}
