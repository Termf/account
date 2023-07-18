package com.binance.account.integration.futureservice;

import com.binance.assetservice.api.ITranApi;
import com.binance.assetservice.vo.request.GetTranRequest;
import com.binance.future.api.AssetTransferApi;
import com.binance.future.api.request.AssetTransferRequest;
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
public class FutureTransferApiClient {
    @Autowired
    private AssetTransferApi assetTransferApi;
    @Autowired
    private ITranApi tranApi;


    public void futureAssetTransfer(AssetTransferRequest assetTransferRequest) throws Exception {
        log.info("FutureTransferApiClient.futureAssetTransfer：request={}", assetTransferRequest);
        APIRequest<AssetTransferRequest> originRequest = new APIRequest<AssetTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        APIResponse<Void> apiResponse = assetTransferApi.assetTransfer(APIRequest.instance(originRequest, assetTransferRequest));
        log.info("FutureTransferApiClient.futureAssetTransfer：request={},response={}", assetTransferRequest, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("FutureTransferApiClient.futureAssetTransfer : error" + apiResponse.getErrorData());
            throw new BusinessException("futureAssetTransfer failed");
        }
    }

    /**
     * 获取tranId
     * */
    public Long getTransIdForFutureTransfer(String recipientUserId,Integer tranType) throws Exception{
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
            log.error("FutureTransferApiClient.getTransIdForFutureTransfer error" + apiResponse.getErrorData());
            throw new BusinessException("getTransIdForFutureTransfer failed");
        }
        return apiResponse.getData();
    }

}
