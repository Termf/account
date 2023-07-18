package com.binance.account.api;

import java.math.BigDecimal;
import java.util.List;

import com.binance.account.vo.subuser.CreateNoEmailSubUserReq;
import com.binance.account.vo.subuser.request.BindingParentSubUserEmailReq;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToMasterResponse;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToSubResponse;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.FuturePositionRiskVO;
import com.binance.account.vo.subuser.response.BindingParentSubUserEmailResp;
import com.binance.account.vo.subuser.response.CreateNoEmailSubUserResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountSummaryResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountSummaryResp;
import com.binance.account.vo.subuser.response.SubAccountFuturesEnableResp;
import com.binance.account.vo.subuser.response.SubAccountMarginEnableResp;
import com.binance.account.vo.subuser.SubAccountTransferHistoryInfoVo;
import com.binance.account.vo.subuser.response.SubAccountTranHisResForSapiVersion;
import com.binance.account.vo.subuser.response.SubAccountTransferHistoryInfoResp;
import com.binance.account.vo.subuser.response.SubUserAssetBtcResponse;
import com.binance.account.vo.subuser.response.SubUserCurrencyBalanceResp;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.SubAccountTransferResponse;
import com.binance.account.vo.subuser.response.CreateSubUserResp;
import com.binance.account.vo.subuser.response.SubAccountResp;
import com.binance.account.vo.subuser.response.SubAccountTransferResp;
import com.binance.account.vo.subuser.response.SubUserEmailVoResp;
import com.binance.account.vo.subuser.response.SubUserInfoResp;
import com.binance.account.vo.subuser.response.SubUserTypeResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by Fei.Huang on 2018/10/19.
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/sub-user")
@Api(value = "母子账号接口-BnbWeb")
public interface SubUserApi {

    @ApiOperation("母账号注册子账号")
    @PostMapping("/creation")
    APIResponse<CreateSubUserResp> createSubUser(@RequestBody() APIRequest<CreateSubUserReq> request) throws Exception;

    @ApiOperation("注册资管子账户")
    @PostMapping("/create/assetsubuser")
    APIResponse<Boolean> createAssetManagerSubUser(@RequestBody() APIRequest<CreateAssetManagerSubUserReq> request) throws Exception;


    @ApiOperation("母账号注册无邮箱子账号")
    @PostMapping("/createNoEmailSubUser")
    APIResponse<CreateNoEmailSubUserResp> createNoEmailSubUser(@RequestBody() APIRequest<CreateNoEmailSubUserReq> request) throws Exception;


    @ApiOperation("根据用户ID返回账号类型(普通|母|子)")
    @PostMapping("/relation/check")
    APIResponse<SubUserTypeResponse> checkRelationByUserId(@RequestBody() APIRequest<UserIdReq> request)
            throws Exception;

    @ApiOperation("根据母账户ID和子账户ID判断是否母子关系")
    @PostMapping("/relation/check/ids")
    APIResponse<Boolean> checkRelationByParentSubUserIds(@RequestBody() APIRequest<BindingParentSubUserReq> request)
            throws Exception;

    @ApiOperation("根据母账户ID和子账户email判断是否母子关系并且返回subUserId")
    @PostMapping("/relation/check/subEmail")
    APIResponse<BindingParentSubUserEmailResp> checkRelationByParentSubUserEmail(@RequestBody() APIRequest<BindingParentSubUserEmailReq> request)
            throws Exception;

    @ApiOperation("非子账户，或已被母账户启用的子账户")
    @PostMapping("/status/check/non-sub-user/enabled-sub-user")
    APIResponse<Boolean> notSubUserOrIsEnabledSubUser(@RequestBody() APIRequest<UserIdReq> request) throws Exception;

    @ApiOperation("启用子账号")
    @PostMapping("/status/enable")
    APIResponse<Integer> enableSubUser(@RequestBody() APIRequest<OpenOrCloseSubUserReq> request) throws Exception;

    @ApiOperation("禁用子账户")
    @PostMapping("/status/disable")
    APIResponse<Integer> disableSubUser(@RequestBody() APIRequest<OpenOrCloseSubUserReq> request) throws Exception;

    @ApiOperation("重置子账户2fa")
    @PostMapping("/security/2fa/reset")
    APIResponse<Integer> resetSecondValidation(@RequestBody APIRequest<ResetSecondValidationRequest> request)
            throws Exception;

    @ApiOperation("修改子账号密码")
    @PostMapping("/security/pwd/update")
    APIResponse<Integer> updateSubUserPwd(@RequestBody() APIRequest<UpdatePassWordRequest> request) throws Exception;

    @ApiOperation("子账号信息列表")
    @PostMapping("/info/list")
    APIResponse<SubUserInfoResp> selectSubUserInfo(@RequestBody() APIRequest<QuerySubUserRequest> request)
            throws Exception;

    @ApiOperation("母账户查询子账户列表")
    @PostMapping("/info/sub-account/list")
    APIResponse<List<SubAccountResp>> getSubAccountList(@RequestBody() APIRequest<SubUserSearchReq> request)
            throws Exception;

    @ApiOperation("母账户查询子母账户划转历史")
    @PostMapping("/info/sub-account/transfer/history")
    APIResponse<List<SubAccountTransferResp>> getSubAccountTransferHistory(@RequestBody() APIRequest<SubAccountTransHisReq> request)
            throws Exception;

    @ApiOperation("母账户查询子母账户划转历史的详细信息")
    @PostMapping("/info/sub-account/transfer/historyInfo")
    APIResponse<SubAccountTransferHistoryInfoResp> getSubAccountTransferHistoryInfo(@RequestBody() APIRequest<SubAccountTransHistoryInfoReq> request)
            throws Exception;

    @ApiOperation("查母账户下的所有子账户邮箱(弃用!)")
    @PostMapping("/all/email")
    APIResponse<SubUserEmailVoResp> selectSubUserEmailList(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("子账户登录历史记录列表")
    @PostMapping("/login/history")
    APIResponse<GetUserSecurityLogResponse> loginHistoryList(@RequestBody() APIRequest<SubUserSecurityLogReq> request) throws Exception;


    @ApiOperation("子母账户划转接口")
    @PostMapping("/accountTransfer")
    APIResponse<SubAccountTransferResponse> subAccountTransfer(@RequestBody() APIRequest<SubAccountTransferRequest> request) throws Exception;


    @ApiOperation("母账户修改子账户邮箱接口")
    @PostMapping("/modifySubAccount")
    APIResponse<Integer> modifySubAccount(@RequestBody() APIRequest<ModifySubAccountRequest> request) throws Exception;


    @ApiOperation("子账户开通Margin")
    @PostMapping("/margin/enable")
    APIResponse<SubAccountMarginEnableResp> subAccountMarginEnable(@RequestBody() APIRequest<SubAccountMarginEnableRequest> request)
            throws Exception;

    @ApiOperation("子账户开通Futures")
    @PostMapping("/futures/enable")
    APIResponse<SubAccountFuturesEnableResp> subAccountFuturesEnable(@RequestBody() APIRequest<SubAccountFuturesEnableRequest> request)
            throws Exception;


    @ApiOperation("查询子账户的Futures账户详情")
    @PostMapping("/query/futures/account")
    APIResponse<QuerySubAccountFutureAccountResp> queryFuturesAccount(@RequestBody() APIRequest<QuerySubAccountFutureAccountRequest> request)
            throws Exception;


    @ApiOperation("查询子账户的Futuress账户汇总")
    @PostMapping("/query/futures/accountSummary")
    APIResponse<QuerySubAccountFutureAccountSummaryResp> queryFuturesAccountSummary(@RequestBody() APIRequest<QuerySubAccountFutureAccountSummaryRequest> request)
            throws Exception;


    @ApiOperation("查询子账户持仓信息")
    @PostMapping("/query/futures/positionRisk")
    APIResponse<List<FuturePositionRiskVO>> queryFuturesPositionRisk(@RequestBody() APIRequest<QueryFuturesPositionRiskRequest> request)
            throws Exception;


    @ApiOperation("查询子账户的margin账户汇总")
    @PostMapping("/query/margin/accountSummary")
    APIResponse<QuerySubAccountMarginAccountSummaryResp> queryMarginAccountSummary(@RequestBody() APIRequest<QuerySubAccountMarginAccountSummaryRequest> request)
            throws Exception;

    @ApiOperation("查询子账户的margin账户详情")
    @PostMapping("/query/margin/account")
    APIResponse<QuerySubAccountMarginAccountResp> queryMarginAccount(@RequestBody() APIRequest<QuerySubAccountMarginAccountRequest> request)
            throws Exception;

    @ApiOperation("母账户重发子账户激活邮件")
    @PostMapping("/resendRegisterMail")
    APIResponse<ResendSendActiveCodeResponse> resendSubUserRegisterMail(@RequestBody() APIRequest<ResendSubUserRegisterMailReq> request) throws Exception;

    @ApiOperation("子账户列表及子账户BTC总值")
    @PostMapping("/subUserAssetBtcList")
    APIResponse<SubUserAssetBtcResponse> subUserAssetBtcList(@RequestBody() APIRequest<SubUserAssetBtcRequest> request) throws Exception;

    @ApiOperation("母账户BTC总值")
    @PostMapping("/parentUserAssetBtc")
    APIResponse<BigDecimal> parentUserAssetBtc(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("母账户下所有子账户总资产折合BTC数")
    @PostMapping("/allSubUserAssetBtc")
    APIResponse<BigDecimal> allSubUserAssetBtc(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    @ApiOperation("查询子账户相应币种的可用余额")
    @PostMapping("/subUserCurrencyBalance")
    APIResponse<List<SubUserCurrencyBalanceResp>> subUserCurrencyBalance(@RequestBody() APIRequest<SubUserCurrencyBalanceReq> request) throws Exception;

    @ApiOperation("母账户根据tranId查询子母账户划转记录")
    @PostMapping("/info/sub-account/transferByTranId")
    APIResponse<SubAccountTransferHistoryInfoVo> getSubUserTransferByTranId(@RequestBody() APIRequest<SubUserTransferByTranIdReq> request) throws Exception;


    @ApiOperation("检查字母账号关系以及是否存在future账号")
    @PostMapping("/relation/future/check")
    APIResponse<Long> checkRelationAndFutureAccountEnable(@Validated @RequestBody() APIRequest<QuerySubAccountFutureAccountRequest> request) throws Exception;


    @ApiOperation("子母账户划转接口（子账户划转给子账户)")
    @PostMapping("/subAccountTransferVersionForSubToSub")
    APIResponse<SubAccountTransferVersionForSubToSubResponse> subAccountTransferVersionForSubToSub(@RequestBody() APIRequest<SubAccountTransferVersionForSubToSubRequest> request) throws Exception;


    @ApiOperation("向主账户主动划转（适用子账户）")
    @PostMapping("/subAccountTransferVersionForSubToMaster")
    APIResponse<SubAccountTransferVersionForSubToMasterResponse> subAccountTransferVersionForSubToMaster(@RequestBody() APIRequest<SubAccountTransferVersionForSubToMasterRequest> request) throws Exception;



    @ApiOperation("查询子账户划转历史(适用子账户)")
    @PostMapping("/subUserHistoryVersionForSapi")
    public APIResponse<List<SubAccountTranHisResForSapiVersion>> subUserHistoryVersionForSapi(@RequestBody APIRequest<SubUserHistoryVersionForSapiRequest> request) throws Exception;



    @ApiOperation("修改子账号备注")
    @PostMapping("/updateSubUserRemark")
    APIResponse<Integer> updateSubUserRemark(@RequestBody() APIRequest<UpdateSubUserRemarkRequest> request) throws Exception;


    @ApiOperation("子母账号future划转")
    @PostMapping("/subAccountFutureAssetTransfer")
    APIResponse<Boolean> subAccountFutureAssetTransfer(@Validated @RequestBody APIRequest<SubAccountFutureTransferReq> request)throws Exception;

}
