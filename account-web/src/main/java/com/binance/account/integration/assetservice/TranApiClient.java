package com.binance.account.integration.assetservice;

import java.math.BigDecimal;

import com.alibaba.fastjson.JSONObject;
import com.binance.assetservice.enums.TransferType;
import com.binance.assetservice.vo.request.ByTranIdRequest;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.binance.account.constants.AccountConstants;
import com.binance.assetservice.api.ITranApi;
import com.binance.assetservice.vo.request.AddSubAccountTransferRecordRequest;
import com.binance.assetservice.vo.request.GetSubAccountTransferHistoryRequest;
import com.binance.assetservice.vo.request.GetTranRequest;
import com.binance.assetservice.vo.request.UpdateSubAccountTransferBaseRatesRequest;
import com.binance.assetservice.vo.request.UpdateSubAccountTransferRecordStatusRequest;
import com.binance.assetservice.vo.response.AssetSubAccountTrasnferVo;
import com.binance.assetservice.vo.response.GetSubAccountTransferHistoryResponse;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class TranApiClient {
    @Autowired
    private ITranApi tranApi;
    /**
     * 获取tranId
     * */
    public Long getTransIdForSubAccountTransfer(String recipientUserId) throws Exception{

        APIRequest<GetTranRequest> originRequest = new APIRequest<GetTranRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetTranRequest request = new GetTranRequest();
        request.setTranType(AccountConstants.SUBUSER_ASSET_TRANSFER);
        request.setTime(DateUtils.getNewUTCDate());
        request.setDescription(String.format("To %s", recipientUserId));
        APIResponse<Long> apiResponse = tranApi.getTranId(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.getTransIdForSubAccountTransfer error" + apiResponse.getErrorData());
            throw new BusinessException("getTransIdForSubAccountTransfer failed");
        }
        return apiResponse.getData();
    }

    /**
     * 增加子账户划转日志
     * */
    public Integer addSubAccountTransferRecord(String parentUserId, String senderUserId, String senderEmail, String recipientUserId, String recipientEmail,
                                               String transferAsset, BigDecimal transferAmount, Long transactionId, String status, String thirdTranId)  throws Exception{
        APIRequest<AddSubAccountTransferRecordRequest> originRequest = new APIRequest<AddSubAccountTransferRecordRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        AddSubAccountTransferRecordRequest request = new AddSubAccountTransferRecordRequest();
        request.setSenderUserId(senderUserId);
        request.setSenderEmail(senderEmail);
        request.setRecipientUserId(recipientUserId);
        request.setRecipientEmail(recipientEmail);
        request.setTransferAsset(transferAsset);
        request.setTransferAmount(transferAmount);
        request.setTransactionId(transactionId);
        request.setParentUserId(parentUserId);
        request.setStatus(status);
        request.setThirdTranId(thirdTranId);
        APIResponse<Integer> apiResponse = tranApi.addSubAccountTransferRecord(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.addSubAccountTransferRecord error" + apiResponse.getErrorData());
            throw new BusinessException("addSubAccountTransferRecord failed");
        }
        return apiResponse.getData();
    }


    /**
     * 更新子账户划转日志汇率
     * */
    public Integer recordBaseRates(String transferAsset, Long transactionId)  throws Exception{
        APIRequest<UpdateSubAccountTransferBaseRatesRequest> originRequest = new APIRequest<UpdateSubAccountTransferBaseRatesRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        UpdateSubAccountTransferBaseRatesRequest request = new UpdateSubAccountTransferBaseRatesRequest();
        request.setTransactionId(transactionId);
        request.setTransferAsset(transferAsset);
        APIResponse<Integer> apiResponse = tranApi.updateSubAccountTransferBaseRates(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.warn("TranApiClient.recordBaseRates error" + apiResponse.getErrorData());
            throw new BusinessException("recordBaseRates failed");
        }
        return apiResponse.getData();
    }

    /**
     * 更新子账户划转日志状态
     * */
    public Integer updateSubAccountTransferRecordStatus( Long transactionId,String status)  throws Exception{
        APIRequest<UpdateSubAccountTransferRecordStatusRequest> originRequest = new APIRequest<UpdateSubAccountTransferRecordStatusRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        UpdateSubAccountTransferRecordStatusRequest request = new UpdateSubAccountTransferRecordStatusRequest();
        request.setTransactionId(transactionId);
        request.setStatus(status);
        APIResponse<Integer> apiResponse = tranApi.updateSubAccountTransferRecordStatus(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.updateSubAccountTransferRecordStatus error" + apiResponse.getErrorData());
            throw new BusinessException("updateSubAccountTransferRecordStatus failed");
        }
        return apiResponse.getData();
    }

    /**
     * 查询账户交易历史记录
     * */
    public GetSubAccountTransferHistoryResponse getAccountTransferHistory(GetSubAccountTransferHistoryRequest getSubAccountTransferHistoryRequest)  throws Exception{
        log.info("getAccountTransferHistory.param:{}",getSubAccountTransferHistoryRequest);
        APIRequest<GetSubAccountTransferHistoryRequest> apiRequest = new APIRequest<>();
        apiRequest.setBody(getSubAccountTransferHistoryRequest);
        APIResponse<GetSubAccountTransferHistoryResponse> apiResponse = tranApi.getSubAccountTransferHistory(apiRequest);
        log.info("getAccountTransferHistory.res:{}",JsonUtils.toJsonHasNullKey(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.getAccountTransferHistory error" + apiResponse.getErrorData());
            throw new BusinessException("getAccountTransferHistory failed");
        }
        return apiResponse.getData();
    }

    /**
     * 根据tranId查询母子账户交易记录
     * */
    public AssetSubAccountTrasnferVo getTransferByTranId(Long tranId)  throws Exception{
        log.info("getTransferByTranId.tranId:{}",tranId);
        APIRequest<ByTranIdRequest> apiRequest = new APIRequest<>();
        ByTranIdRequest byTranIdRequest = new ByTranIdRequest();
        byTranIdRequest.setTranId(tranId);
        apiRequest.setBody(byTranIdRequest);
        APIResponse<AssetSubAccountTrasnferVo> apiResponse = tranApi.getTransferByTranId(apiRequest);
        log.info("getTransferByTranId.res:{}",JsonUtils.toJsonHasNullKey(apiResponse));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.getTransferByTranId error" + apiResponse.getErrorData());
            throw new BusinessException("getTransferByTranId failed");
        }
        return apiResponse.getData();
    }

    /**
     * 做市商对公划转获取tranId
     * */
    public Long getTransIdForMarketMakerTransfer(String recipientUserId, Date tranTime) throws Exception{

        APIRequest<GetTranRequest> originRequest = new APIRequest<GetTranRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        GetTranRequest request = new GetTranRequest();
        request.setTranType(TransferType.MAIN_MAIN_TRANSFER.getCode());
        request.setTime(tranTime);
        request.setDescription(String.format("To %s", recipientUserId));
        log.info("getTransIdForMarketMakerTransfer.res:{}", LogMaskUtils.maskJsonString2(JSONObject.toJSONString(request)));
        APIResponse<Long> apiResponse = tranApi.getTranId(APIRequest.instance(originRequest, request));
        log.info("getTransIdForMarketMakerTransfer.res:{}", LogMaskUtils.maskJsonString2(JSONObject.toJSONString(apiResponse)));
        if (apiResponse == null || APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("TranApiClient.getTransIdForMarketMakerTransfer error");
            throw new BusinessException("getTransIdForMarketMakerTransfer failed");
        }
        return apiResponse.getData();
    }
}
