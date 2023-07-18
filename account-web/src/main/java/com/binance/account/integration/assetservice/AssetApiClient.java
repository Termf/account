package com.binance.account.integration.assetservice;

import com.binance.assetservice.api.IAssetApi;
import com.binance.assetservice.vo.request.GetAssetByCodeRequest;
import com.binance.assetservice.vo.response.asset.AssetResponse;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetApiClient {


    @Autowired
    private IAssetApi assetApi;

    /**
     * 获取资产信息
     * */
    public AssetResponse getAssetByCode(String asset)throws Exception{
        APIRequest<GetAssetByCodeRequest> originRequest = new APIRequest<GetAssetByCodeRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetAssetByCodeRequest request = new GetAssetByCodeRequest();
        request.setAsset(asset);
        request.setBackend(false);//不要把测试资产查出来
        APIResponse<AssetResponse> apiResponse = assetApi.getAssetByCode(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("AssetApiClient.getAssetByCode :asset=" + asset + "  error" + apiResponse.getErrorData());
            throw new BusinessException("getAssetByCode failed");
        }
        return apiResponse.getData();
    }
}
