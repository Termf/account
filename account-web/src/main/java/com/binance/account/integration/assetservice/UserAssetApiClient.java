package com.binance.account.integration.assetservice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.assetservice.api.IUserAssetApi;
import com.binance.assetservice.enums.AssetTransferStatus;
import com.binance.assetservice.enums.KindType;
import com.binance.assetservice.vo.request.AssetTransferRequest;
import com.binance.assetservice.vo.request.GetPrivateUserAssetRequest;
import com.binance.assetservice.vo.request.UserAssetTransferBtcRequest;
import com.binance.assetservice.vo.request.UserNegativeAssetRequest;
import com.binance.assetservice.vo.request.WalletAssetTransferAdminRequest;
import com.binance.assetservice.vo.request.asset.SelectByUserIdsCodeRequest;
import com.binance.assetservice.vo.request.asset.SelectOneUserAssetLogRequest;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.assetservice.vo.response.WalletAssetTransferResponse;
import com.binance.assetservice.vo.response.asset.SelectUserAssetLogResponse;
import com.binance.assetservice.vo.response.asset.SelectUserAssetResponse;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserAssetApiClient {
    @Autowired
    private IUserAssetApi userAssetApi;

    public void assetTransfer(String fromUserId, String toUserId, String asset, BigDecimal amount, Long tranId, Integer type) throws Exception{
        APIRequest<AssetTransferRequest> originRequest = new APIRequest<AssetTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        AssetTransferRequest request = new AssetTransferRequest();
        request.setAmount(amount);
        request.setAsset(asset);
        request.setCost(null);
        request.setInfo( String.format("From%s,To%s", fromUserId,toUserId));
        request.setType(type);
        request.setTranId(tranId);
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        APIResponse<Void> apiResponse = userAssetApi.assetTransfer(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("UserAssetApiClient.assetTransfer error" + apiResponse.getErrorData());
            throw new BusinessException("assetTransfer failed");
        }
    }



    /**
     * 获取用户的资产信息
     * */
    public UserAssetResponse getPrivateUserAsset(String userId, String asset)throws Exception{
        APIRequest<GetPrivateUserAssetRequest> originRequest = new APIRequest<GetPrivateUserAssetRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetPrivateUserAssetRequest request = new GetPrivateUserAssetRequest();
        request.setUserId(userId);
        request.setAsset(asset);
        log.info("UserAssetApiClient.getPrivateUserAsset request:{}", JSONObject.toJSONString(request));
        APIResponse<UserAssetResponse> apiResponse = userAssetApi.getPrivateUserAsset(APIRequest.instance(originRequest, request));
        log.info("UserAssetApiClient.getPrivateUserAsset response:{}", JSONObject.toJSONString(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("UserAssetApiClient.getPrivateUserAsset :asset=" + asset + "  error" + apiResponse.getErrorData());
            throw new BusinessException("getPrivateUserAsset failed");
        }
        return apiResponse.getData();
    }

    /**
     * 获取资产为0的asset
     * */
    public Integer queryNegativeAssetByUserId(Long userId)throws Exception{
        APIRequest<UserNegativeAssetRequest> originRequest = new APIRequest<UserNegativeAssetRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        UserNegativeAssetRequest request = new UserNegativeAssetRequest();
        request.setUserId(userId);
        APIResponse<Integer> apiResponse = userAssetApi.queryNegativeAssetByUserId(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("UserAssetApiClient.queryNegativeAssetByUserId :userId=" + userId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("queryNegativeAssetByUserId failed");
        }
        return apiResponse.getData();
    }

    /**
     * 获取用户的BTC资产
     * @param userId
     * @return
     */
    public UserAssetTransferBtcResponse getUserAssetTransferBtc(Long userId) throws Exception {
        UserAssetTransferBtcRequest btcRequest = new UserAssetTransferBtcRequest();
        btcRequest.setUserId(userId.toString());
        APIRequestHeader header = WebUtils.getAPIRequestHeader();
        APIResponse<UserAssetTransferBtcResponse> response = userAssetApi.userAssetTransferBtc(APIRequest.instance(header, btcRequest));
        if (response == null || response.getStatus() != APIResponse.Status.OK) {
            log.info("获取用户的BTC资产估值失败. userId:{} message:{}", userId, JSON.toJSONString(response));
            throw new BusinessException("UserAssetApiClient.getUserAssetTransferBtc fail");
        }
        return response.getData();
    }

    /**
     * 查询用户相应币种的资产
     * @param userIds
     * @param asset
     * @return
     * @throws Exception
     */
    public List<SelectUserAssetResponse> getUserAssetByUserIdsCode (List<Long> userIds, String asset) throws Exception {
        APIRequestHeader header = WebUtils.getAPIRequestHeader();
        SelectByUserIdsCodeRequest request = new SelectByUserIdsCodeRequest();
        request.setAsset(asset);
        request.setUserIds(userIds);
        APIResponse<List<SelectUserAssetResponse>> apiResponse = userAssetApi.getUserAssetByUserIdsCode(APIRequest.instance(header, request));
        if (apiResponse == null || apiResponse.getStatus() != APIResponse.Status.OK) {
            log.info("获取用户资产失败. asset:{} userIds:{} message:{}", asset, userIds, JSON.toJSONString(apiResponse));
            throw new BusinessException("UserAssetApiClient.getUserAssetByUserIdsCode fail");
        }
        return apiResponse.getData();
    }


    /**
     * 获取用户指定币种的BTC资产
     * @param userId
     * @return
     */
    public UserAssetTransferBtcResponse getUserAssetTransferBtcByAsset(Long userId,String asset) throws Exception {
        UserAssetTransferBtcRequest btcRequest = new UserAssetTransferBtcRequest();
        btcRequest.setUserId(userId.toString());
        btcRequest.setAsset(asset);
        APIRequestHeader header = WebUtils.getAPIRequestHeader();
        APIResponse<UserAssetTransferBtcResponse> response = userAssetApi.userAssetTransferBtc(APIRequest.instance(header, btcRequest));
        if (response == null || response.getStatus() != APIResponse.Status.OK) {
            log.info("getUserAssetTransferBtcByAsset. userId:{} message:{}", userId, JSON.toJSONString(response));
            throw new BusinessException("UserAssetApiClient.getUserAssetTransferBtcByAsset fail");
        }
        return response.getData();
    }

    /**
     * asset提供的新的划转接口
     * @throws Exception
     */
    public void walletAssetTransferAdmin(String fromUserId, String toUserId, String asset, BigDecimal amount, Long tranId, Date tranTime) throws Exception{
        APIRequest<AssetTransferRequest> originRequest = new APIRequest<AssetTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        WalletAssetTransferAdminRequest request = new WalletAssetTransferAdminRequest();
        request.setKindType(KindType.MAIN_MAIN_TRANSFER);
        request.setTranId(tranId);
        request.setTranTime(tranTime.getTime());
        request.setAmount(amount);
        request.setAsset(asset);
        request.setInfo( String.format("From%s,To%s", fromUserId,toUserId));
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        log.info("UserAssetApiClient.walletAssetTransferAdmin request: {}", LogMaskUtils.maskJsonString2(JSONObject.toJSONString(request)));
        APIResponse<WalletAssetTransferResponse> apiResponse = userAssetApi.walletAssetTransferAdmin(APIRequest.instance(originRequest, request));
        log.info("UserAssetApiClient.walletAssetTransferAdmin response: {}", LogMaskUtils.maskJsonString2(JSONObject.toJSONString(apiResponse)));
        if (apiResponse == null || APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("UserAssetApiClient.walletAssetTransferAdmin error");
            throw new BusinessException("walletAssetTransferAdmin error");
        }
        WalletAssetTransferResponse response = apiResponse.getData();
        if (response == null || !AssetTransferStatus.isSuccess(response.getFromStatus().getKey()) || !AssetTransferStatus.isSuccess(response.getToStatus().getKey())) {
            log.error("UserAssetApiClient.walletAssetTransferAdmin failed: {}");
            throw new BusinessException(String.format("walletAssetTransferAdmin failed: %s, %s", response.getFromErrorMessage(), response.getToErrorMessage()));    
        }
    }

    /**
     * 查资产日志信息
     * */
    public SelectUserAssetLogResponse getAssetLogByParam(Long userId, Long tranId, String asset, Integer type)throws Exception{
        APIRequest<SelectOneUserAssetLogRequest> originRequest = new APIRequest<SelectOneUserAssetLogRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        SelectOneUserAssetLogRequest request = new SelectOneUserAssetLogRequest();
        request.setUserId(userId);
        request.setAsset(asset);
        request.setTranId(tranId);
        request.setType(type);
        log.info("UserAssetApiClient.getAssetLogByParam request:{}", JSONObject.toJSONString(request));

        APIResponse<SelectUserAssetLogResponse> apiResponse = userAssetApi.getAssetLogByParam(APIRequest.instance(originRequest, request));
        log.info("UserAssetApiClient.getAssetLogByParam response:{}", JSONObject.toJSONString(apiResponse));
        if (apiResponse == null || APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("UserAssetApiClient.getAssetLogByParam userId:" +userId+ ":asset=" + asset + "  error" + apiResponse.getErrorData());
            throw new BusinessException("getAssetLogByParam failed");
        }
        return apiResponse.getData();
    }

}
