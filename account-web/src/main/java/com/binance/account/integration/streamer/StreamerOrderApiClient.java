package com.binance.account.integration.streamer;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.streamer.api.order.OrderApi;
import com.binance.streamer.api.promo.PromoApi;
import com.binance.streamer.api.request.order.QueryOpenOrderIdsRequest;
import com.binance.streamer.api.request.order.QueryOrdersRequest;
import com.binance.streamer.api.request.promo.QueryHasOrderReq;
import com.binance.streamer.api.request.trade.GetAgentAndUserTradesRequest;
import com.binance.streamer.api.request.trade.GetByAgentUserIdBatchRequest;
import com.binance.streamer.api.response.SearchResult;
import com.binance.streamer.api.response.order.QueryOpenOrderIdsResponse;
import com.binance.streamer.api.response.trade.GetAgentAndUserTradesResponse;
import com.binance.streamer.api.response.trade.GetByAgentUserIdBatchResponse;
import com.binance.streamer.api.response.vo.GetAgentAndUserTradesVo;
import com.binance.streamer.api.response.vo.GetByAgentUserIdBatchVo;
import com.binance.streamer.api.response.vo.OpenOrderVo;
import com.binance.streamer.api.response.vo.OrderListVo;
import com.binance.streamer.api.trade.TradeApi;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@Service
public class StreamerOrderApiClient {
    @Resource
    private OrderApi orderApi;
    @Autowired
    private TradeApi tradeApi;
    @Resource
    private PromoApi promoApi;


    public List<OpenOrderVo> selectOpenOrderIds(Long userId, String symbol, List<String> symbols, String price, String side){
        APIRequest<QueryOpenOrderIdsRequest> originRequest = new APIRequest<QueryOpenOrderIdsRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        QueryOpenOrderIdsRequest request = new QueryOpenOrderIdsRequest();
        request.setUserId(userId);
        request.setSymbol(symbol);
        request.setSymbols(symbols);
        request.setPrice(price);
        request.setSide(side);
        log.info("StreamerOrderApiClient.selectOpenOrderIds start：request={}", request);
        APIResponse<QueryOpenOrderIdsResponse> apiResponse = orderApi.selectOpenOrderIds(APIRequest.instance(originRequest, request));
        log.info("StreamerOrderApiClient.selectOpenOrderIds end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("StreamerOrderApiClient.selectOpenOrderIds : error" + apiResponse.getErrorData());
            throw new BusinessException("selectOpenOrderIds failed");
        }
        return apiResponse.getData().getOpenOrderIds();
    }

   public List<OpenOrderVo> selectOpenOrderOnlyByUserId(Long userId){
        return selectOpenOrderIds(userId,null,null,null,null);
    }


    public List<GetAgentAndUserTradesVo> getUserByAgentIdAndTradeIds(GetAgentAndUserTradesRequest getAgentAndUserTradesRequest){
        log.info("StreamerOrderApiClient.getUserByAgentIdAndTradeIds.getAgentAndUserTradesRequest:{}",JsonUtils.toJsonHasNullKey(getAgentAndUserTradesRequest));
        APIRequest<GetAgentAndUserTradesRequest> apiRequest = new APIRequest<GetAgentAndUserTradesRequest>();
        apiRequest.setBody(getAgentAndUserTradesRequest);
        try{
            APIResponse<GetAgentAndUserTradesResponse> apiResponse = tradeApi.getAgentAndUserTrades(apiRequest);
            log.info("StreamerOrderApiClient.getUserByAgentIdAndTradeIds :request=" + JsonUtils.toJsonHasNullKey(getAgentAndUserTradesRequest) + "  error:" + apiResponse.getErrorData());
            if (APIResponse.Status.OK == apiResponse.getStatus() && apiResponse.getData() != null) {
                return apiResponse.getData().getAgentUserIdList();
            }
            log.error("StreamerOrderApiClient.getUserByAgentIdAndTradeIds :request=" + JsonUtils.toJsonHasNullKey(getAgentAndUserTradesRequest) + "  error:" + apiResponse.getErrorData());
            return Lists.newArrayList();
        }catch (Exception e){
            log.error("StreamerOrderApiClient.getUserByAgentIdAndTradeIds :request=" + JsonUtils.toJsonHasNullKey(getAgentAndUserTradesRequest) + "  error:" + e);
            throw new BusinessException("getUserByAgentIdAndTradeIds failed");
        }
    }

    public SearchResult<OrderListVo> selectOrders(Long userId, Long accountId, String symbol, Integer page, Integer rows){
        APIRequest<QueryOrdersRequest> originRequest = new APIRequest<QueryOrdersRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        QueryOrdersRequest request = new QueryOrdersRequest();
        request.setUserId(userId);
        request.setAccountId(accountId);
        request.setSymbol(symbol);
        request.setPage(page);
        request.setRows(rows);
        log.info("StreamerOrderApiClient.selectOrders start：request={}", request);
        APIResponse<SearchResult<OrderListVo>> apiResponse = orderApi.selectOrders(APIRequest.instance(originRequest, request));
        log.info("StreamerOrderApiClient.selectOrders end ：request={},response={}", request, apiResponse);
        if (apiResponse == null || APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("StreamerOrderApiClient.selectOrders : error" + apiResponse.getErrorData());
            throw new BusinessException("selectOrders failed");
        }
        return apiResponse.getData();
    }

    public Boolean hasOrder(Long userId, Long endTime){
        APIRequest<QueryHasOrderReq> originRequest = new APIRequest<QueryHasOrderReq>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        QueryHasOrderReq request = new QueryHasOrderReq();
        request.setUserId(userId);
        request.setEndTime(endTime);
        log.info("StreamerOrderApiClient.hasOrder start：request={}", request);
        APIResponse<Boolean> apiResponse = promoApi.hasOrder(APIRequest.instance(originRequest, request));
        log.info("StreamerOrderApiClient.hasOrder end ：request={},response={}", request, apiResponse);
        if (apiResponse == null || APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("StreamerOrderApiClient.hasOrder : error" + apiResponse.getErrorData());
            throw new BusinessException("hasOrder failed");
        }
        return apiResponse.getData();
    }
}
