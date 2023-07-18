package com.binance.account.api;

import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.security.request.SecurityStatusRequest;
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
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Created by yangyang on 2019/8/19.
 */
@Api(value = "broker母子账号接口")
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/broker/sub-user")
public interface BrokerApi {

    @ApiOperation("创建broker的subsuer")
    @PostMapping("/createSubUser")
    public APIResponse<CreateBrokerSubUserResp> createBrokerSubUser(@RequestBody() APIRequest<CreateBrokerSubUserReq> request) throws Exception;


    @ApiOperation("Enable Margin for Sub Account")
    @PostMapping("/enableMargin")
    public APIResponse<CreateMarginForBrokerSubUserResp> createMarginForBrokerSubUser(@RequestBody() APIRequest<CreateMarginForBrokerSubUserReq> request)
            throws Exception;


    @ApiOperation("Enable futures for Sub Account")
    @PostMapping("/enableFutures")
    public APIResponse<CreateFuturesForBrokerSubUserResp> createFuturesForBrokerSubUser(@RequestBody() APIRequest<CreateFuturesForBrokerSubUserReq> request)
            throws Exception;

    @ApiOperation("创建broker的subsuerapi")
    @PostMapping("/createSubUserApi")
    public APIResponse<CreateBrokerSubUserApiRes> createBrokerSubUserApi(@RequestBody() APIRequest<CreateBrokerSubUserApiReq> request)
            throws Exception;

    @ApiOperation("删除broker的subsuer")
    @PostMapping("/deleteSubUserApi")
    public APIResponse deleteBrokerSubApiKey(@RequestBody APIRequest<DeleteBrokerSubUserApiReq> request) throws Exception;

    @ApiOperation("查询broker的subsuer")
    @PostMapping("/queryBrokerSubApiKey")
    public APIResponse<List<QueryBrokerSubUserApiRes>> queryBrokerSubApiKey(@RequestBody APIRequest<QueryBrokerSubUserApiReq> request) throws Exception;

    @ApiOperation("更新broker的subsuer交易权限")
    @PostMapping("/updateBrokerSubApiPermission")
    public APIResponse<CreateBrokerSubUserApiRes> updateBrokerSubApiPermission(@RequestBody APIRequest<UpdateBrokerSubUserApiReq> request) throws Exception;

    @ApiOperation("查询broker的subsueraccount")
    @PostMapping("/queryBrokerSubAccount")
    public APIResponse<List<QueryBrokerSubAccountRes>> queryBrokerSubAccount(@RequestBody APIRequest<QueryBrokerSubAccountReq> request) throws Exception;

    @ApiOperation("更新broker的subsueraccount-commisssion")
    @PostMapping("/changeBrokerSubuserCommission")
    public APIResponse<ChangeBrokerSubUserCommissionRes> changeBrokerSubuserCommission(@RequestBody APIRequest<ChangeBrokerSubUserCommissionReq> request)
            throws Exception;

    @ApiOperation("查询broker的ueraccount-commisssion")
    @PostMapping("/queryBrokerUserCommission")
    public APIResponse<BrokerUserCommissionRes> queryBrokerUserCommission(@RequestBody APIRequest<BrokerUserCommissionReq> request) throws Exception;


    @ApiOperation("broker中子账户划转")
    @PostMapping("/subAccountTransfer")
    public APIResponse<BrokerSubAccountTransferResponse> subAccountTransfer(@RequestBody APIRequest<BrokerSubAccountTransferRequest> request) throws Exception;


    @ApiOperation("broker中子账户划转历史记录")
    @PostMapping("/brokerSubAccountTransferHistory")
    public APIResponse<List<BrokerSubAccountTranHisRes>> brokerSubAccountTransferHistory(@RequestBody APIRequest<BrokerSubAccountTransHistoryReq> request) throws Exception;

    @ApiOperation("修改期货账号手续费")
    @PostMapping("/commission/futures")
    public APIResponse<BrokerCommissionFuturesResponse> commissionFutures(@RequestBody APIRequest<BrokerCommissionFuturesRequest> request) throws Exception;

    @ApiOperation("查询期货账号手续费")
    @PostMapping("/query/commissionFutures")
    public APIResponse<List<BrokerQueryCommissionFuturesResponse>> queryCommissionFutures(@RequestBody APIRequest<BrokerQueryCommissionFuturesRequest> request) throws Exception;

    @ApiOperation("新建三方昵称-api返佣推荐方")
    @PostMapping("/createApiAgentAlias")
    public APIResponse<CreateApiAgentAliasRes> createApiAgentAlias(@RequestBody() @Validated APIRequest<CreateApiAgentAliasReq> request)
            throws Exception;

    @ApiOperation("查询三方昵称-api返佣推荐方")
    @PostMapping("/selectApiAgentAlias")
    public APIResponse<List<SelectApiAgentCodeAliasRes>> selectApiAgentAlias(@RequestBody() @Validated APIRequest<SelectApiAgentAliasReq> request)
            throws Exception;

    @ApiOperation("新建三方昵称-普通用户")
    @PostMapping("/createApiAgentAliasByAgentCode")
    public APIResponse<CreateApiAgentAliasByAgentCodeRes> createApiAgentAliasByAgentCode(@RequestBody() @Validated APIRequest<CreateApiAgentAliasByAgentCodeReq> request)
            throws Exception;

    @ApiOperation("查询三方昵称-普通用户")
    @PostMapping("/selectApiAgentAliasByAgentCode")
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAgentAliasByAgentCode(@RequestBody() @Validated APIRequest<SelectApiAgentAliasByAgentCodeReq> request)
            throws Exception;

    @ApiOperation("查询一个三方refereeID")
    @PostMapping("/selectApiAliasByAgentAndCustomer")
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAliasByAgentAndCustomer(APIRequest<SelectApiAgentAliasReq> request)
        throws Exception;

    @ApiOperation("查询api返佣")
    @PostMapping("/selectApiAgentCommissionDetail")
    public APIResponse<List<SelectApiAgentCommissionDetailRes>> selectApiAgentCommissionDetail(@RequestBody() @Validated APIRequest<SelectApiAgentCommissionDetailReq> request)
            throws Exception;

    @ApiOperation("更新broker渠道")
    @PostMapping("/updateBrokerSource")
    public APIResponse<Boolean> updateBrokerSource(@RequestBody @Validated APIRequest<UpdateBrokerUserCommissionSourceReq> request)throws Exception;

    @ApiOperation("开启关闭broker子账户bnb燃烧开关")
    @PostMapping("/query/updateBrokerSubBNBBurnSwitch")
    public APIResponse<BrokerBnbBurnSwitchResp> updateBrokerSubBNBBurnSwitch(@RequestBody @Validated APIRequest<BrokerBnbBurnSwitchRequest> request) throws Exception;

    @ApiOperation("开启关闭broker子账户maring的bnb抵扣手续费开关")
    @PostMapping("/query/updateBrokerSubMarginInterestBNBBurnSwitch")
    public APIResponse<BrokerSubMarginInterestBnbBurnSwitchResp> updateBrokerSubMarginInterestBNBBurnSwitch(@RequestBody @Validated APIRequest<BrokerSubMarginInterestBnbBurnSwitchRequest> request) throws Exception;

    @ApiOperation("查询broker子账户的bnbburn状态-包含marign利息")
    @PostMapping("/query/selectBrokerSubBnbBurnStatus")
    public APIResponse<SelectBrokerSubBnbBurnStatusResp> selectBrokerSubBnbBurnStatus(@RequestBody @Validated APIRequest<SelectBrokerSubBnbBurnStatusRequest> request) throws Exception;



    @ApiOperation("修改broker划转开关")
    @PostMapping("/updateBrokerTransferSwitch")
    APIResponse<UpdateBrokerTransferSwitchResponse> updateBrokerTransferSwitch(@Validated @RequestBody APIRequest<UpdateBrokerTransferSwitchRequest> request)
            throws Exception;
    @ApiOperation("查询broker返佣")
    @PostMapping("/selectBrokerCommissionDetail")
    public APIResponse<List<SelectBrokerCommissionDetailRes>> selectBrokerCommissionDetail(APIRequest<SelectBrokerCommissionDetailReq> request)
            throws Exception;

    @ApiOperation("查询brokersubUserId")
    @PostMapping("/queryBrokerSubUserIdBySubAccount")
    public APIResponse<QueryBrokerSubUserBySubAccountRes> queryBrokerSubUserIdBySubAccount(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception;

    @ApiOperation("查询broker子账户Spot资产")
    @PostMapping("/queryBrokerSubAccountSpotAsset")
    APIResponse<List<SubAccountSpotAssetResp>> queryBrokerSubAccountSpotAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception;

    @ApiOperation("查询broker子账户Margin资产")
    @PostMapping("/queryBrokerSubAccountMarginAsset")
    APIResponse<List<SubAccountMarginAssetResp>> queryBrokerSubAccountMarginAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception;

    @ApiOperation("查询broker子账户Futures资产")
    @PostMapping("/queryBrokerSubAccountFuturesAsset")
    APIResponse<List<SubAccountFuturesAssetResp>> queryBrokerSubAccountFuturesAsset(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception;

    @ApiOperation("查询broker子账户的subAccountId")
    @PostMapping("/queryBrokerSubAccountId")
    APIResponse<List<QueryBrokerSubAccountIdResponse>> queryBrokerSubAccountId(@RequestBody @Validated APIRequest<QueryBrokerSubAccountIdRequest> request)
            throws Exception;


    @ApiOperation("check 是否是broker子母账号关系并且返回subuser信息")
    @PostMapping("/checkRelationShipAndReturnSubUser")
    APIResponse<CheckRelationShipAndReturnSubUserResp> checkRelationShipAndReturnSubUser(@RequestBody @Validated APIRequest<CheckRelationShipAndReturnSubUserReq> request)
            throws Exception;

    @ApiOperation("查询broker子账户的subAccountId")
    @PostMapping("/brokerFutureAssetTransfer")
    APIResponse<Boolean> brokerFutureAssetTransfer(@RequestBody @Validated APIRequest<BrokerFutureTransferReq> request)throws Exception;

    @ApiOperation("check 是否是broker子母账号关系并且返回subuserbinding信息")
    @PostMapping("/checkAndGetBrokerSubUserBindings")
    APIResponse<List<BrokerSubUserBindingsResp>> checkAndGetBrokerSubUserBindings(@RequestBody @Validated APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception;


    @ApiOperation("查询broker子账户的brokerSubAccountId")
    @PostMapping("/queryByBrokerSubAccountId")
    APIResponse<QueryByBrokerSubAccountIdResponse> queryByBrokerSubAccountId(@RequestBody @Validated APIRequest<QueryByBrokerSubAccountIdRequest> request)
            throws Exception;



    @ApiOperation("获取子母账户关系信息")
    @PostMapping("/getSubBindingInfo")
    APIResponse<GetrSubUserBindingsResp> getSubBindingInfo(@RequestBody @Validated APIRequest<GetSubbindingInfoReq> request)
            throws Exception;

    @ApiOperation("修改期货交割（币本位 ）账号手续费")
    @PostMapping("/commission/deliveryfutures")
    public APIResponse<UpdateBrokerCommissionDeliveryResponse> commissionDeliveryFutures(@RequestBody@Validated APIRequest<UpdateBrokerCommissionDeliveryRequest> request) throws Exception;


    @ApiOperation("查询期货账号手续费")
    @PostMapping("/query/commissiondeliveryFutures")
    public APIResponse<List<BrokerQueryCommissionDeliveryFuturesResponse>> queryCommissionDeliveryFutures(@RequestBody APIRequest<BrokerQueryCommissionDeliveryFuturesRequest> request) throws Exception;


    @ApiOperation("获取子母账户关系信息分页")
    @PostMapping("/getSubBindingInfoByPage")
    APIResponse<GetSubBindingInfoByPageResp> getSubBindingInfoByPage(@RequestBody @Validated APIRequest<GetSubBindingInfoByPageReq> request)
            throws Exception;

}
