package com.binance.account.integration.margin;

import com.binance.margin.api.bookkeeper.MarginAccountApi;
import com.binance.margin.api.bookkeeper.request.CreateMarginAccountRequest;
import com.binance.margin.api.bookkeeper.request.MajorUidRequest;
import com.binance.margin.api.bookkeeper.request.MarginUidRequest;
import com.binance.margin.api.bookkeeper.request.MarginUidsRequest;
import com.binance.margin.api.bookkeeper.response.AccountDetailResponse;
import com.binance.margin.api.bookkeeper.response.AccountInfoResponse;
import com.binance.margin.api.bookkeeper.response.AccountSimpleResponse;
import com.binance.margin.api.bookkeeper.response.AccountSummaryResponse;
import com.binance.margin.api.profit.enums.PeriodType;
import com.binance.margin.api.transfer.TransferApi;
import com.binance.margin.api.transfer.request.TransferRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Log4j2
@Service
public class MarginAccountApiClient {

    @Autowired
    private MarginAccountApi marginAccountApi;
    @Autowired
    private TransferApi transferApi;

    public boolean updateTradeStatus(Long marginUid, boolean enableStatus){
        log.info("MarginAccountApiClient.updateTradeStatus.marginUid:{},enableStatus:{}",marginUid,enableStatus);
        MarginUidRequest marginUidRequest = new MarginUidRequest();
        marginUidRequest.setMarginUid(marginUid);
        APIResponse<Void> response = null;
        if (enableStatus){
            response = marginAccountApi.enableMarginAccount(APIRequest.instance(marginUidRequest));
        }else{
            response = marginAccountApi.disableMarginAccount(APIRequest.instance(marginUidRequest));
        }
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("MarginAccountApiClient.updateTradeStatus :marginUid=" + marginUid + "  error" + response.getErrorData());
            throw new BusinessException("updateTradeStatus failed");
        }
        return true;
    }


    public void newMarginAccount( Long rootUserId, Long rootTradingAccount, Long marginUserId, Long marginTradingAccount,Boolean isSubUser){
        APIRequest<CreateMarginAccountRequest> originRequest = new APIRequest<CreateMarginAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        CreateMarginAccountRequest request = new CreateMarginAccountRequest();
        request.setMajorUid(rootUserId);
        request.setMajorAccountId(rootTradingAccount);
        request.setMarginUid(marginUserId);
        request.setMarginAccountId(marginTradingAccount);
        request.setSubAccount(isSubUser);
        APIResponse<Void> apiResponse = marginAccountApi.newMarginAccount(APIRequest.instance(originRequest, request));
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAccountApiClient.newMarginAccount :rootUserId=" + rootUserId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("newMarginAccount failed");
        }
    }

    public Long marginTransfer(Long userId, String asset, BigDecimal amount,Integer type){
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setUserId(userId);
        transferRequest.setAsset(asset);
        transferRequest.setAmount(amount);
        APIResponse<Long> response =  null;
        // main to margin
        if (type == 1){
            response = transferApi.rollIn(APIRequest.instance(transferRequest));
        }else{
            // margin to main
            response = transferApi.rollOut(APIRequest.instance(transferRequest));
        }
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("MarginAccountApiClient.marginTransfer.userId:{},asset:{},amount:{},type:{}",userId,asset,amount,type);
            throw new BusinessException("marginTransfer failed");
        }
        return response.getData();
    }

    public void enableOrdisableMarignInterest(Long rootUserId,boolean interestSwitch){
        log.info("MarginAccountApiClient.enableOrdisableMarignInterest.rootUserId:{},interestSwitch:{}",rootUserId,interestSwitch);
        MajorUidRequest majorUidRequest = new MajorUidRequest();
        majorUidRequest.setMajorUid(rootUserId);
        APIResponse<Void> response = null;
        if (interestSwitch){
            response = marginAccountApi.enableMarginAccountBnbDiscount(APIRequest.instance(majorUidRequest));
        }else{
            response = marginAccountApi.disableMarginAccountBnbDiscount(APIRequest.instance(majorUidRequest));
        }
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("MarginAccountApiClient.enableOrdisableMarignInterest :marginUserId=" + rootUserId + "  error" + response.getErrorData());
            throw new BusinessException("enableOrdisableMarignInterest failed");
        }
    }

    public AccountDetailResponse selecteMarignAccountDetail(Long marginUserId){
        log.info("MarginAccountApiClient.selecteMarignAccountDetail.marginUserId:{}",marginUserId);
        APIResponse<AccountDetailResponse> response = marginAccountApi.accountDetails(marginUserId);
        if (APIResponse.Status.ERROR == response.getStatus() || response.getData() == null) {
            log.error("MarginAccountApiClient.selecteMarignAccountDetail :marginUserId=" + marginUserId + "  error" + response.getErrorData());
            throw new BusinessException("selecteMarignAccountDetail failed");
        }
        return response.getData();
    }


    public AccountSummaryResponse subAccountSummary(Long parentUserId, List<Long> subUserIds,List<Long> paginateSubUserIds){
        APIRequest<MarginUidsRequest> originRequest = new APIRequest<MarginUidsRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        MarginUidsRequest request = new MarginUidsRequest();
        request.setMasterUserId(parentUserId);
        request.setSubUserIds(subUserIds);
        request.setPaginateSubUserIds(paginateSubUserIds);
        log.info("MarginAccountApiClient.subAccountSummary start：request={}", request);
        APIResponse<AccountSummaryResponse> apiResponse = marginAccountApi.subAccountSummary(APIRequest.instance(originRequest, request));
        log.info("MarginAccountApiClient.subAccountSummary end ：request={},response={}", request, apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAccountApiClient.subAccountSummary :parentUserId=" + parentUserId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("subAccountSummary failed");
        }
        return apiResponse.getData();
    }

    public AccountInfoResponse subAccountInfo(Long userId, PeriodType periodType){
        log.info("MarginAccountApiClient.subAccountInfo start：userId={},periodType={}", userId,periodType);
        APIResponse<AccountInfoResponse> apiResponse = marginAccountApi.subAccountInfo(userId,periodType);

        log.info("MarginAccountApiClient.subAccountInfo end ：response={}", apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAccountApiClient.subAccountInfo :userId=" + userId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("subAccountInfo failed");
        }
        return apiResponse.getData();
    }

    public AccountSimpleResponse accountSimpleList(Long userId){
        log.info("MarginAccountApiClient.accountSimpleList start：userId={}",userId);
        APIResponse<List<AccountSimpleResponse>> apiResponse = marginAccountApi.accountSimpleList(String.valueOf(userId));

        log.info("MarginAccountApiClient.accountSimpleList end ：response={}", apiResponse);
        if (APIResponse.Status.ERROR == apiResponse.getStatus()) {
            log.error("MarginAccountApiClient.accountSimpleList :userId=" + userId + "  error" + apiResponse.getErrorData());
            throw new BusinessException("accountSimpleList failed");
        }
        if (CollectionUtils.isNotEmpty(apiResponse.getData()) && apiResponse.getData().size() > 0){
            return apiResponse.getData().get(0);
        }
        return null;
    }
}
