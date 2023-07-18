package com.binance.account.integration.mbxgateway;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.binance.mbxgateway.api.IOrderApi;
import com.binance.mbxgateway.vo.request.order.DeleteAllOrderRequest;
import com.binance.mbxgateway.vo.request.order.DeleteOrderRequest;
import com.binance.mbxgateway.vo.request.order.DeleteOrderResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class MbxgatewayIOrderApiClient {
    @Autowired
    private IOrderApi orderApi;

    public DeleteOrderResponse mDeleteOrder(String userId, List<String> symbols, List<String> orderIds) throws Exception{
        APIRequest<DeleteOrderRequest> originRequest = new APIRequest<DeleteOrderRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        DeleteOrderRequest request = new DeleteOrderRequest();
        request.setUserId(userId);
        request.setSymbols(symbols);
        request.setOrderIds(orderIds);
        log.info("MbxgatewayIOrderApiClient.mDeleteOrder start：request={}", request);
        APIResponse<DeleteOrderResponse> apiResponse = orderApi.mDeleteOrder(APIRequest.instance(originRequest, request));
        log.info("MbxgatewayIOrderApiClient.mDeleteOrder end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MbxgatewayIOrderApiClient.mDeleteOrder : error" + apiResponse.getErrorData());
            throw new BusinessException("mDeleteOrder failed");
        }
        return apiResponse.getData();
    }

    public DeleteOrderResponse mDeleteOrderOnlyByUserId(String userId)throws Exception{
        return mDeleteOrder(userId,null,null);
    }

    public void deleteAllOrders(String userId) throws Exception {
        APIRequest<DeleteAllOrderRequest> originRequest = new APIRequest<DeleteAllOrderRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        DeleteAllOrderRequest request = new DeleteAllOrderRequest();
        request.setUserId(userId);
        log.info("MbxgatewayIOrderApiClient.deleteAllOrder start：request={}", request);
        APIResponse<DeleteOrderResponse> apiResponse = orderApi.deleteAllOrder(APIRequest.instance(originRequest, request));
        log.info("MbxgatewayIOrderApiClient.deleteAllOrder end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MbxgatewayIOrderApiClient.deleteAllOrder : error" + apiResponse.getErrorData());
            throw new BusinessException("deleteAllOrder failed");
        }
    }
}
