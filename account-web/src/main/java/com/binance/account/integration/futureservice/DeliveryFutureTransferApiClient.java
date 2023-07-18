package com.binance.account.integration.futureservice;

import com.binance.assetservice.api.ITranApi;
import com.binance.assetservice.vo.request.GetTranRequest;
import com.binance.delivery.periphery.api.DeliveryAssetTransferApi;
import com.binance.delivery.periphery.api.request.core.AssetTransferRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class DeliveryFutureTransferApiClient {
    @Autowired
    private DeliveryAssetTransferApi deliveryAssetTransferApi;
    @Autowired
    private ITranApi tranApi;


    public void deliveryFutureAssetTransfer(AssetTransferRequest assetTransferRequest) throws Exception {
        log.info("DeliveryFutureTransferApiClient.futureAssetTransfer：request={}", assetTransferRequest);
        APIRequest<AssetTransferRequest> originRequest = new APIRequest<AssetTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        APIResponse<Void> apiResponse = deliveryAssetTransferApi.assetTransfer(APIRequest.instance(originRequest, assetTransferRequest));
        log.info("DeliveryFutureTransferApiClient.deliveryFutureAssetTransfer：request={},response={}", assetTransferRequest, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("DeliveryFutureTransferApiClient.deliveryFutureAssetTransfer : error" + apiResponse.getErrorData());
            throw new BusinessException("deliveryFutureAssetTransfer failed");
        }
    }

    /**
     * 获取tranId
     * */
    public Long getDeliveryTransIdForFutureTransfer(String recipientUserId,Integer tranType) throws Exception{
        APIRequest<GetTranRequest> originRequest = new APIRequest<GetTranRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetTranRequest request = new GetTranRequest();
        request.setTranType(tranType);
        request.setTime(DateUtils.getNewUTCDate());
        request.setDescription(String.format("To %s", recipientUserId));
        APIResponse<Long> apiResponse = tranApi.getTranId(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus() || apiResponse.getData() == null) {
            log.error("DeliveryFutureTransferApiClient.getDeliveryTransIdForFutureTransfer error" + apiResponse.getErrorData());
            throw new BusinessException("getDeliveryTransIdForFutureTransfer failed");
        }
        return apiResponse.getData();
    }

}
