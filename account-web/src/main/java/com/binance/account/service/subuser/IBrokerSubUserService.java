package com.binance.account.service.subuser;

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
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.List;

/**
 * Created by yangyang on 2019/8/19.
 */
public interface IBrokerSubUserService {

    APIResponse<CreateBrokerSubUserResp> createBrokerSubUser(APIRequest<CreateBrokerSubUserReq> request) throws Exception;

    APIResponse<CreateMarginForBrokerSubUserResp> createMarginForBrokerSubUser(APIRequest<CreateMarginForBrokerSubUserReq> request) throws Exception;

    APIResponse<CreateFuturesForBrokerSubUserResp> createFuturesForBrokerSubUser(APIRequest<CreateFuturesForBrokerSubUserReq> request) throws Exception;

    APIResponse<CreateBrokerSubUserApiRes> createBrokerSubUserApi(APIRequest<CreateBrokerSubUserApiReq> request)throws Exception;

    void deleteBrokerSubApiKey(APIRequest<DeleteBrokerSubUserApiReq> request) throws Exception;

    APIResponse<List<QueryBrokerSubUserApiRes>> queryBrokerSubApiKey(APIRequest<QueryBrokerSubUserApiReq> request) throws Exception;

    APIResponse<CreateBrokerSubUserApiRes> updateBrokerSubApiPermission(APIRequest<UpdateBrokerSubUserApiReq> request) throws Exception;

    APIResponse<List<QueryBrokerSubAccountRes>> queryBrokerSubAccount(APIRequest<QueryBrokerSubAccountReq> request) throws Exception;

    APIResponse<ChangeBrokerSubUserCommissionRes> changeBrokerSubuserCommission(APIRequest<ChangeBrokerSubUserCommissionReq> request) throws Exception;

    APIResponse<BrokerUserCommissionRes> queryBrokerUserCommission(APIRequest<BrokerUserCommissionReq> request) throws Exception;

    APIResponse<BrokerSubAccountTransferResponse> subAccountTransfer(APIRequest<BrokerSubAccountTransferRequest> request) throws Exception;

    APIResponse<List<BrokerSubAccountTranHisRes>> brokerSubAccountTransferHistory(APIRequest<BrokerSubAccountTransHistoryReq> request) throws Exception;

    APIResponse<BrokerCommissionFuturesResponse> commissionFutures(APIRequest<BrokerCommissionFuturesRequest> request) throws Exception;

    APIResponse<List<BrokerQueryCommissionFuturesResponse>> queryCommissionFutures(APIRequest<BrokerQueryCommissionFuturesRequest> request) throws Exception;

    APIResponse<CreateApiAgentAliasRes> createApiAgentAlias(APIRequest<CreateApiAgentAliasReq> request) throws Exception;

    APIResponse<List<SelectApiAgentCodeAliasRes>> selectApiAgentAlias(APIRequest<SelectApiAgentAliasReq> request)throws Exception;

    APIResponse<List<SelectApiAgentCommissionDetailRes>> selectApiAgentCommissionDetail(APIRequest<SelectApiAgentCommissionDetailReq> request)throws Exception;

    APIResponse<CreateApiAgentAliasByAgentCodeRes> createApiAgentAliasByAgentCode(APIRequest<CreateApiAgentAliasByAgentCodeReq> request)throws Exception;
    APIResponse<BrokerBnbBurnSwitchResp> updateBrokerSubBNBBurnSwitch(APIRequest<BrokerBnbBurnSwitchRequest> request)throws Exception;
    APIResponse<UpdateBrokerTransferSwitchResponse> updateBrokerTransferSwitch(APIRequest<UpdateBrokerTransferSwitchRequest> request) throws Exception;



    APIResponse<BrokerSubMarginInterestBnbBurnSwitchResp> updateBrokerSubMarginInterestBNBBurnSwitch(APIRequest<BrokerSubMarginInterestBnbBurnSwitchRequest> request)throws Exception;

    APIResponse<SelectBrokerSubBnbBurnStatusResp> selectBrokerSubBnbBurnStatus(APIRequest<SelectBrokerSubBnbBurnStatusRequest> request)throws Exception;
    APIResponse<SelectApiAgentCodeAliasRes> selectApiAgentAliasByAgentCode(APIRequest<SelectApiAgentAliasByAgentCodeReq> request)throws Exception;

    APIResponse<SelectApiAgentCodeAliasRes> selectApiAliasByAgentAndCustomer(APIRequest<SelectApiAgentAliasReq> request)throws Exception;

    APIResponse<List<SelectBrokerCommissionDetailRes>> selectBrokerCommissionDetail(APIRequest<SelectBrokerCommissionDetailReq> request)throws Exception;

    APIResponse<QueryBrokerSubUserBySubAccountRes> queryBrokerSubUserIdBySubAccount(APIRequest<QueryBrokerSubAccountReq> request)throws Exception;


    APIResponse<List<SubAccountSpotAssetResp>> queryBrokerSubAccountSpotAsset(APIRequest<QueryBrokerSubAccountReq> request) throws Exception;

    APIResponse<List<SubAccountMarginAssetResp>> queryBrokerSubAccountMarginAsset(APIRequest<QueryBrokerSubAccountReq> request) throws Exception;

    APIResponse<List<SubAccountFuturesAssetResp>> queryBrokerSubAccountFuturesAsset(APIRequest<QueryBrokerSubAccountReq> request) throws Exception;

    APIResponse<List<QueryBrokerSubAccountIdResponse>> queryBrokerSubAccountId(APIRequest<QueryBrokerSubAccountIdRequest> request) throws Exception;

    APIResponse<Boolean> updateBrokerSource(APIRequest<UpdateBrokerUserCommissionSourceReq> request)throws Exception;





    APIResponse<CheckRelationShipAndReturnSubUserResp> checkRelationShipAndReturnSubUser(APIRequest<CheckRelationShipAndReturnSubUserReq> request)throws Exception;


    APIResponse<Boolean> brokerFutureAssetTransfer(APIRequest<BrokerFutureTransferReq> request)throws Exception;
    APIResponse<List<BrokerSubUserBindingsResp>> checkAndGetBrokerSubUserBindings(APIRequest<QueryBrokerSubAccountReq> request);
    APIResponse<QueryByBrokerSubAccountIdResponse> queryByBrokerSubAccountId(APIRequest<QueryByBrokerSubAccountIdRequest> request)throws Exception;


    APIResponse<GetrSubUserBindingsResp> getSubBindingInfo(APIRequest<GetSubbindingInfoReq> request);

    APIResponse<UpdateBrokerCommissionDeliveryResponse> commissionDeliveryFutures(APIRequest<UpdateBrokerCommissionDeliveryRequest> request)throws Exception;

    APIResponse<List<BrokerQueryCommissionDeliveryFuturesResponse>> queryCommissionDeliveryFutures(APIRequest<BrokerQueryCommissionDeliveryFuturesRequest> request)throws Exception;

    APIResponse<GetSubBindingInfoByPageResp> getSubBindingInfoByPage(APIRequest<GetSubBindingInfoByPageReq> request);


}
