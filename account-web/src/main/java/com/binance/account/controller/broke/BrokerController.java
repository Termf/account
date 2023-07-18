package com.binance.account.controller.broke;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.binance.account.aop.SecurityLog;
import com.binance.account.api.BrokerApi;
import com.binance.account.constants.AccountConstants;
import com.binance.account.service.subuser.IBrokerSubUserService;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.subuser.request.BrokerCommissionFuturesRequest;
import com.binance.account.vo.subuser.request.BrokerQueryCommissionFuturesRequest;
import com.binance.account.vo.subuser.request.BrokerSubAccountTransHistoryReq;
import com.binance.account.vo.subuser.request.BrokerSubAccountTransferRequest;
import com.binance.account.vo.subuser.request.BrokerUserCommissionReq;
import com.binance.account.vo.subuser.request.ChangeBrokerSubUserCommissionReq;
import com.binance.account.vo.subuser.request.CreateBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.CreateBrokerSubUserReq;
import com.binance.account.vo.subuser.request.CreateFuturesForBrokerSubUserReq;
import com.binance.account.vo.subuser.request.CreateMarginForBrokerSubUserReq;
import com.binance.account.vo.subuser.request.DeleteBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.QueryBrokerSubAccountReq;
import com.binance.account.vo.subuser.request.QueryBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.UpdateBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.UpdateBrokerTransferSwitchRequest;
import com.binance.account.vo.subuser.response.BrokerCommissionFuturesResponse;
import com.binance.account.vo.subuser.response.BrokerQueryCommissionFuturesResponse;
import com.binance.account.vo.subuser.response.BrokerSubAccountTranHisRes;
import com.binance.account.vo.subuser.response.BrokerSubAccountTransferResponse;
import com.binance.account.vo.subuser.response.BrokerUserCommissionRes;
import com.binance.account.vo.subuser.response.ChangeBrokerSubUserCommissionRes;
import com.binance.account.vo.subuser.response.CreateBrokerSubUserApiRes;
import com.binance.account.vo.subuser.response.CreateBrokerSubUserResp;
import com.binance.account.vo.subuser.response.CreateFuturesForBrokerSubUserResp;
import com.binance.account.vo.subuser.response.CreateMarginForBrokerSubUserResp;
import com.binance.account.vo.subuser.response.CreateSubUserResp;
import com.binance.account.vo.subuser.response.QueryBrokerSubAccountRes;
import com.binance.account.vo.subuser.response.QueryBrokerSubUserApiRes;
import com.binance.account.vo.subuser.response.UpdateBrokerTransferSwitchResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.platform.common.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by yangyang on 2019/8/19.
 */
@RestController
@Log4j2
public class BrokerController implements BrokerApi {

    @Autowired
    private IBrokerSubUserService brokerSubUserService;

    private ExecutorService executorService= new ThreadPoolExecutor(10, 10,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue(100));


    @Value("${broker.query.subtranslog.timeout:10000}")
    private int brokerQuerySubTransLogTimeOut;

    @Value("${broker.query.subtranslog.black:600}")
    private int brokerQuerySubTransLogBlack;


    @Override
    public APIResponse<CreateBrokerSubUserResp> createBrokerSubUser(@RequestBody() @Validated APIRequest<CreateBrokerSubUserReq> request)
            throws Exception {
        return brokerSubUserService.createBrokerSubUser(request);
    }

    @Override
    public APIResponse<CreateMarginForBrokerSubUserResp> createMarginForBrokerSubUser(@RequestBody() @Validated APIRequest<CreateMarginForBrokerSubUserReq> request) throws Exception {
        return brokerSubUserService.createMarginForBrokerSubUser(request);
    }

    @Override
    public APIResponse<CreateFuturesForBrokerSubUserResp> createFuturesForBrokerSubUser(@RequestBody() @Validated APIRequest<CreateFuturesForBrokerSubUserReq> request) throws Exception {
        return brokerSubUserService.createFuturesForBrokerSubUser(request);
    }

    @Override
    public APIResponse<CreateBrokerSubUserApiRes> createBrokerSubUserApi(@RequestBody() @Validated APIRequest<CreateBrokerSubUserApiReq> request)
            throws Exception {
        return brokerSubUserService.createBrokerSubUserApi(request);
    }

    @Override
    public APIResponse deleteBrokerSubApiKey(@RequestBody @Validated APIRequest<DeleteBrokerSubUserApiReq> request) throws Exception{
         brokerSubUserService.deleteBrokerSubApiKey(request);
         return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<List<QueryBrokerSubUserApiRes>> queryBrokerSubApiKey(@RequestBody @Validated APIRequest<QueryBrokerSubUserApiReq> request) throws Exception{
        return brokerSubUserService.queryBrokerSubApiKey(request);
    }

    @Override
    public APIResponse<CreateBrokerSubUserApiRes> updateBrokerSubApiPermission(@RequestBody @Validated APIRequest<UpdateBrokerSubUserApiReq> request) throws Exception{
        return brokerSubUserService.updateBrokerSubApiPermission(request);
    }

    @Override
    public APIResponse<List<QueryBrokerSubAccountRes>> queryBrokerSubAccount(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception{
        return brokerSubUserService.queryBrokerSubAccount(request);
    }

    @Override
    public APIResponse<ChangeBrokerSubUserCommissionRes> changeBrokerSubuserCommission(@RequestBody @Validated APIRequest<ChangeBrokerSubUserCommissionReq> request) throws Exception{
        return brokerSubUserService.changeBrokerSubuserCommission(request);
    }

    @Override
    public APIResponse<BrokerUserCommissionRes> queryBrokerUserCommission(@RequestBody @Validated APIRequest<BrokerUserCommissionReq> request) throws Exception{
        return brokerSubUserService.queryBrokerUserCommission(request);
    }

    @Override
    public APIResponse<BrokerSubAccountTransferResponse> subAccountTransfer(@RequestBody @Validated APIRequest<BrokerSubAccountTransferRequest> request) throws Exception{
        return brokerSubUserService.subAccountTransfer(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/brokerSubAccountTransferHistory")
    public APIResponse<List<BrokerSubAccountTranHisRes>> brokerSubAccountTransferHistory(@RequestBody @Validated APIRequest<BrokerSubAccountTransHistoryReq> request) throws Exception{
        String mainThreadTraceId = StringUtils.isBlank(TrackingUtils.getTrace()) ? TrackingUtils.generateUUID() : TrackingUtils.getTrace();
        String limitKey=request.getBody().getParentUserId().toString()+request.getBody().getSubAccountId().toString();
        String frequencyLimits =
                RedisCacheUtils.get(limitKey, String.class, AccountConstants.ACCOUNT_BROKER_SUBTRANS_BLACK);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        log.info("brokerSubAccountTransferHistory:mainThreadTraceId={}",mainThreadTraceId);
        Future<APIResponse<List<BrokerSubAccountTranHisRes>>> futureresponse=executorService.submit(new Callable<APIResponse<List<BrokerSubAccountTranHisRes>>>() {
            @Override
            public APIResponse<List<BrokerSubAccountTranHisRes>> call() throws Exception {
                String traceId = StringUtils.isBlank(TrackingUtils.getTrace()) ? TrackingUtils.generateUUID() : TrackingUtils.getTrace();
                TrackingUtils.saveTrace(traceId);
                log.info("mainThreadTraceId={},currentThreadId={}",mainThreadTraceId,traceId);
                APIResponse<List<BrokerSubAccountTranHisRes>> response=brokerSubUserService.brokerSubAccountTransferHistory(request);
                TrackingUtils.clearTrace();
                return response;
            }
        });
        APIResponse<List<BrokerSubAccountTranHisRes>> apiResponse=null;
        try{
            //10s超时拿不到就返回
            apiResponse= futureresponse.get(brokerQuerySubTransLogTimeOut,TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            log.error("brokerSubAccountTransferHistory timeout");
            RedisCacheUtils.set(limitKey, limitKey, brokerQuerySubTransLogBlack, AccountConstants.ACCOUNT_BROKER_SUBTRANS_BLACK);
            throw new BusinessException(GeneralCode.TOO_MANY_REQUESTS);
        }catch (Exception e){
            if(e.getCause() instanceof BusinessException){
                BusinessException businessException=  (BusinessException)e.getCause();
                throw businessException;
            }
            throw e;
        }
        return apiResponse;
    }

    @Override
    public APIResponse<BrokerCommissionFuturesResponse> commissionFutures(@RequestBody @Validated APIRequest<BrokerCommissionFuturesRequest> request) throws Exception {
        return brokerSubUserService.commissionFutures(request);
    }

    @Override
    public APIResponse<UpdateBrokerCommissionDeliveryResponse> commissionDeliveryFutures(@RequestBody@Validated APIRequest<UpdateBrokerCommissionDeliveryRequest> request) throws Exception{
        return brokerSubUserService.commissionDeliveryFutures(request);
    }

    @Override
    public APIResponse<List<BrokerQueryCommissionFuturesResponse>> queryCommissionFutures(@RequestBody @Validated APIRequest<BrokerQueryCommissionFuturesRequest> request) throws Exception {
        return brokerSubUserService.queryCommissionFutures(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/query/commissiondeliveryFutures")
    public APIResponse<List<BrokerQueryCommissionDeliveryFuturesResponse>> queryCommissionDeliveryFutures(@RequestBody APIRequest<BrokerQueryCommissionDeliveryFuturesRequest> request) throws Exception{
        return brokerSubUserService.queryCommissionDeliveryFutures(request);
    }

    @Override
    public APIResponse<CreateApiAgentAliasRes> createApiAgentAlias(@RequestBody() @Validated APIRequest<CreateApiAgentAliasReq> request)
            throws Exception {
        return brokerSubUserService.createApiAgentAlias(request);
    }

    @Override
    public APIResponse<BrokerBnbBurnSwitchResp> updateBrokerSubBNBBurnSwitch(@RequestBody @Validated APIRequest<BrokerBnbBurnSwitchRequest> request) throws Exception {
        return brokerSubUserService.updateBrokerSubBNBBurnSwitch(request);
    }

    @Override
    public APIResponse<List<SelectApiAgentCodeAliasRes>> selectApiAgentAlias(@RequestBody() @Validated APIRequest<SelectApiAgentAliasReq> request)
            throws Exception {
        return brokerSubUserService.selectApiAgentAlias(request);
    }

    @Override
    public APIResponse<CreateApiAgentAliasByAgentCodeRes> createApiAgentAliasByAgentCode(@RequestBody() @Validated APIRequest<CreateApiAgentAliasByAgentCodeReq> request)
            throws Exception{
        return brokerSubUserService.createApiAgentAliasByAgentCode(request);
    }

    @Override
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAgentAliasByAgentCode(@RequestBody() @Validated APIRequest<SelectApiAgentAliasByAgentCodeReq> request)
            throws Exception{
        return brokerSubUserService.selectApiAgentAliasByAgentCode(request);
    }

    @Override
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAliasByAgentAndCustomer(@RequestBody() @Validated APIRequest<SelectApiAgentAliasReq> request)
            throws Exception {
        return brokerSubUserService.selectApiAliasByAgentAndCustomer(request);
    }

    @Override
    public APIResponse<List<SelectApiAgentCommissionDetailRes>> selectApiAgentCommissionDetail(@RequestBody() @Validated APIRequest<SelectApiAgentCommissionDetailReq> request)
            throws Exception {
        return brokerSubUserService.selectApiAgentCommissionDetail(request);
    }

    @Override
    public APIResponse<List<SelectBrokerCommissionDetailRes>> selectBrokerCommissionDetail(@RequestBody @Validated APIRequest<SelectBrokerCommissionDetailReq> request)throws Exception{
        return brokerSubUserService.selectBrokerCommissionDetail(request);
    }

    @Override
    public APIResponse<QueryBrokerSubUserBySubAccountRes> queryBrokerSubUserIdBySubAccount(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception{
        return brokerSubUserService.queryBrokerSubUserIdBySubAccount(request);
    }

    @Override
    public APIResponse<Boolean> updateBrokerSource(@RequestBody @Validated APIRequest<UpdateBrokerUserCommissionSourceReq> request)throws Exception{
        return brokerSubUserService.updateBrokerSource(request);
    }

    @Override
    public APIResponse<BrokerSubMarginInterestBnbBurnSwitchResp> updateBrokerSubMarginInterestBNBBurnSwitch(@RequestBody @Validated APIRequest<BrokerSubMarginInterestBnbBurnSwitchRequest> request) throws Exception{
        return brokerSubUserService.updateBrokerSubMarginInterestBNBBurnSwitch(request);
    }

    @Override
    public APIResponse<SelectBrokerSubBnbBurnStatusResp> selectBrokerSubBnbBurnStatus(@RequestBody @Validated APIRequest<SelectBrokerSubBnbBurnStatusRequest> request) throws Exception{
        return brokerSubUserService.selectBrokerSubBnbBurnStatus(request);
    }

    @Override
    @SecurityLog(name = "修改broker划转状态", operateType = AccountConstants.UPDATE_BROKER_TRANSFER_STATUS,
            userId = "#request.body.userId")
    public APIResponse<UpdateBrokerTransferSwitchResponse> updateBrokerTransferSwitch(@RequestBody @Validated APIRequest<UpdateBrokerTransferSwitchRequest> request) throws Exception {
        return brokerSubUserService.updateBrokerTransferSwitch(request);
    }

    @Override
    public APIResponse<List<SubAccountSpotAssetResp>> queryBrokerSubAccountSpotAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        return brokerSubUserService.queryBrokerSubAccountSpotAsset(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/queryBrokerSubAccountMarginAsset")
    public APIResponse<List<SubAccountMarginAssetResp>> queryBrokerSubAccountMarginAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        return brokerSubUserService.queryBrokerSubAccountMarginAsset(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/queryBrokerSubAccountFuturesAsset")
    public APIResponse<List<SubAccountFuturesAssetResp>> queryBrokerSubAccountFuturesAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        return brokerSubUserService.queryBrokerSubAccountFuturesAsset(request);
    }

    @Override
    public APIResponse<List<QueryBrokerSubAccountIdResponse>> queryBrokerSubAccountId(@RequestBody @Validated APIRequest<QueryBrokerSubAccountIdRequest> request) throws Exception {
        return brokerSubUserService.queryBrokerSubAccountId(request);
    }

    @Override
    public APIResponse<CheckRelationShipAndReturnSubUserResp> checkRelationShipAndReturnSubUser(@RequestBody @Validated APIRequest<CheckRelationShipAndReturnSubUserReq> request) throws Exception {
        return brokerSubUserService.checkRelationShipAndReturnSubUser(request);
    }

    @Override
    public APIResponse<Boolean> brokerFutureAssetTransfer(@RequestBody @Validated APIRequest<BrokerFutureTransferReq> request)throws Exception{
        return brokerSubUserService.brokerFutureAssetTransfer(request);
    }

    @Override
    public APIResponse<List<BrokerSubUserBindingsResp>> checkAndGetBrokerSubUserBindings(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        return brokerSubUserService.checkAndGetBrokerSubUserBindings(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/queryByBrokerSubAccountId")
    public APIResponse<QueryByBrokerSubAccountIdResponse> queryByBrokerSubAccountId(@RequestBody @Validated APIRequest<QueryByBrokerSubAccountIdRequest> request)
            throws Exception{
        return brokerSubUserService.queryByBrokerSubAccountId(request);
    }

    @Override
    public APIResponse<GetrSubUserBindingsResp> getSubBindingInfo(@RequestBody @Validated APIRequest<GetSubbindingInfoReq> request) throws Exception {
        return brokerSubUserService.getSubBindingInfo(request);
    }

    @Override
    @SentinelResource(value = "/broker/sub-user/getSubBindingInfoByPage")
    public APIResponse<GetSubBindingInfoByPageResp> getSubBindingInfoByPage(@RequestBody @Validated APIRequest<GetSubBindingInfoByPageReq> request) throws Exception {
        return brokerSubUserService.getSubBindingInfoByPage(request);
    }
}
