package com.binance.account.controller.user;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.binance.account.aop.MarginValidate;
import com.binance.account.aop.SubUserValidate;
import com.binance.account.vo.subuser.request.SelectIfHasAgenUserRequest;
import com.binance.account.vo.user.UserGroupVo;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.AccountUnifyUserInfoResponse;
import com.binance.account.vo.user.response.UserParentOrRootRelationShipByUserIdResp;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserInfoApi;
import com.binance.account.service.user.IUserInfo;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.response.BatchUpdateAgentResponse;
import com.binance.account.vo.user.response.GetUserAgentRewardResponse;
import com.binance.account.vo.user.response.ResultDateResponse;
import com.binance.account.vo.user.response.UserCommissionResponse;
import com.binance.account.vo.user.response.UserConfigResponse;
import com.binance.account.vo.user.response.UserInfoRewardRatioResponse;
import com.binance.account.vo.user.request.AccountUnifyUserInfoRequest;
import com.binance.account.vo.user.request.AgentRewardAuditRequest;
import com.binance.account.vo.user.request.AgentRewardRequest;
import com.binance.account.vo.user.request.AgentStatusByBatchIdRequest;
import com.binance.account.vo.user.request.AgentStatusRequest;
import com.binance.account.vo.user.request.BaseDetailRequest;
import com.binance.account.vo.user.request.SelectSubFutureUserIdsRequest;
import com.binance.account.vo.user.request.SelectUserConfigRequest;
import com.binance.account.vo.user.request.SetCommissionRequest;
import com.binance.account.vo.user.request.SetTradeAutoStatus;
import com.binance.account.vo.user.request.SetTradeLevelAndCommissionRequest;
import com.binance.account.vo.user.request.SetTradeLevelRequest;
import com.binance.account.vo.user.request.SetUserConfigRequest;
import com.binance.account.vo.user.request.UpdateAgentRewardRatioRequest;
import com.binance.account.vo.user.request.UpdateDailyFiatWithdrawCapRequest;
import com.binance.account.vo.user.request.UpdateDailyWithdrawCapRequest;
import com.binance.account.vo.user.request.UpdateUserAgentRewardListRequest;
import com.binance.account.vo.user.request.UpdateUserAgentRewardRequest;
import com.binance.account.vo.user.request.UpdateUserInfoByUserIdRequest;
import com.binance.account.vo.user.request.UserAgentIdRequest;
import com.binance.account.vo.user.request.UserFutureAgentIdRequest;
import com.binance.account.vo.user.request.UserIdRequest;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

@RestController
public class UserInfoController implements UserInfoApi {

    @Resource
    private IUserInfo iUserInfo;

    @Override
    public APIResponse<Integer> updateUserInfoByUserId(
            @Validated() @RequestBody() APIRequest<UpdateUserInfoByUserIdRequest> request) throws Exception {
        return iUserInfo.updateUserInfoByUserId(request);
    }

    @Override
    public APIResponse<Integer> updateAgentRewardRatio(
            @Validated() @RequestBody() APIRequest<UpdateAgentRewardRatioRequest> request) throws Exception {
        return this.iUserInfo.updateAgentRewardRatio(request);
    }

    @Override
    public APIResponse<Integer> updateUserAgentRatio(@Validated @RequestBody APIRequest<UpdateUserAgentRewardRequest> request) throws Exception {
        return this.iUserInfo.updateUserAgentRatio(request);
    }
    @MarginValidate
    @SubUserValidate
    @Override
    public APIResponse<Integer> setCommission(@Validated @RequestBody APIRequest<SetCommissionRequest> request)
            throws Exception {
        return this.iUserInfo.setCommission(request);
    }
    @MarginValidate
    @SubUserValidate
    @Override
    public APIResponse<Integer> setTradeLevel(@Validated @RequestBody APIRequest<SetTradeLevelRequest> request) throws Exception {
        return iUserInfo.setTradeLevel(request);
    }

    @Override
    public APIResponse<Integer> setTradeAutoStatus(@Validated @RequestBody APIRequest<SetTradeAutoStatus> request) throws Exception{
        return iUserInfo.setTradeAutoStatus(request);
    }
    @MarginValidate
    @Override
    public APIResponse<Integer> setTradeLevelAndCommissionRequest(@Validated @RequestBody APIRequest<SetTradeLevelAndCommissionRequest> request) throws Exception {
        return iUserInfo.setTradeLevelAndCommissionRequest(request);
    }

    @Override
    public APIResponse<Integer> updatePnkTradingAccount() throws Exception {
        return iUserInfo.updatePnkTradingAccount();
    }

    @Override
    public APIResponse<Integer> saveUserConfig(@Validated @RequestBody APIRequest<SetUserConfigRequest> request) throws Exception {
        return iUserInfo.saveUserConfig(request);
    }

    @Override
    public APIResponse<List<UserConfigResponse>> selectUserConfig(@Validated @RequestBody APIRequest<SelectUserConfigRequest> request) throws Exception {
        return iUserInfo.selectUserConfig(request);
    }

    @Override
    public APIResponse<GetUserAgentRewardResponse> getUserAgentList(@Validated @RequestBody APIRequest<AgentRewardRequest> request) throws Exception {
        return iUserInfo.getUserAgentList(request);
    }

    @Override
    public APIResponse<Integer> updateAgentId(@Validated @RequestBody APIRequest<UserAgentIdRequest> request) throws Exception {
        return iUserInfo.updateAgentId(request);
    }

    @Override
    public APIResponse<Integer> updateFutureAgentId(@Validated @RequestBody APIRequest<UserFutureAgentIdRequest> request) throws Exception {
        return iUserInfo.updateFutureAgentId(request);
    }

    @Override
    public APIResponse<List<Long>> selectSubFutureUserIds(@Validated @RequestBody APIRequest<SelectSubFutureUserIdsRequest> request) throws Exception {
        return iUserInfo.selectSubFutureUserIds(request);
    }

    @Override
    public APIResponse<BatchUpdateAgentResponse> batchUpdateAgentId(@Validated @RequestBody APIRequest<UpdateUserAgentRewardListRequest> request)
            throws Exception {
        return iUserInfo.batchUpdateAgentId(request);
    }

    @Override
    public APIResponse<GetUserAgentRewardResponse> getUserAgentRewardList(@Validated @RequestBody APIRequest<AgentRewardAuditRequest> request)
            throws Exception {
        return iUserInfo.getUserAgentRewardList(request);
    }

    @Override
    public APIResponse<ResultDateResponse> updateAgentStatus(@Validated @RequestBody APIRequest<AgentStatusRequest> request)
            throws Exception {
        return iUserInfo.updateAgentStatus(request);
    }

    @Override
    public APIResponse<ResultDateResponse> updateAgentStatusByBatchId(@Validated @RequestBody APIRequest<AgentStatusByBatchIdRequest> request)
            throws Exception {
        return iUserInfo.updateAgentStatusByBatchId(request);
    }

    @Override
    public APIResponse<AuditAgentStatusByBatchIdResponse> auditAgentStatusByBatchId(APIRequest<AuditAgentStatusByBatchIdRequest> request) throws Exception {
        return iUserInfo.auditAgentStatusByBatchId(request);
    }

    @Override
    public APIResponse<List<Long>> selectAgentUserIdByBatchIds(@Validated @RequestBody APIRequest<AgentStatusByBatchIdRequest> request)
            throws Exception {
        return iUserInfo.selectAgentUserIdByBatchIds(request);
    }

    @Override
    public APIResponse<UserInfoRewardRatioResponse> selectUserInfoRewardRatio(@Validated @RequestBody APIRequest<BaseDetailRequest> request)
            throws Exception {
        return iUserInfo.selectUserInfoRewardRatio(request);
    }

    @Override
    public APIResponse<UserInfoVo> getUserInfoByUserId(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return iUserInfo.getUserInfoByUserId(request);
    }

    @Override
    public APIResponse<List<UserInfoVo>> getUserInfosByUserIds(@Validated @RequestBody APIRequest<List<Long>> request) throws Exception {
        return iUserInfo.getUserInfosByUserIds(request);
    }

    @Override
    public APIResponse<UserCommissionResponse> getUserCommission(
            @Validated @RequestBody APIRequest<IdLongRequest> request) throws Exception {
        return iUserInfo.getUserCommission(request);
    }

    @Override
    public APIResponse<List<BigDecimal>> getUserLevelWithdreaw(
            @Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return iUserInfo.getUserLevelWithdreaw(request);
    }

    @Override
    public APIResponse<Integer> updateUserDailyWithdrawCap(@Validated @RequestBody APIRequest<UpdateDailyWithdrawCapRequest> request)
            throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.updateUserDailyWithdrawCap(request.getBody()));
    }

    @Override
    public APIResponse<BigDecimal> getUserDailyWithdrawCap(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.getUserDailyWithdrawCap(request.getBody()));
    }

    @Override
    public APIResponse<Integer> updateUserDailyFiatWithdrawCap(@Validated @RequestBody APIRequest<UpdateDailyFiatWithdrawCapRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.updateUserDailyFiatWithdrawCap(request.getBody()));
    }

    @Override
    public APIResponse<String> getUserLastLoginLanguage(Long userId) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.getUserLastLoginLanguage(userId));
    }

    @Override
    public APIResponse<List<UserGroupVo>> allUserGroup(@Validated @RequestBody APIRequest<APIRequest.VoidBody> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.allUserGroup());
    }

    @Override
    public APIResponse<List<Long>> batchUpdateUserTradeLevelAndCommission(@Validated @RequestBody APIRequest<List<SetTradeLevelAndCommissionRequest>> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.batchUpdateUserTradeLevelAndCommission(request.getBody()));
    }

    @Override
    public APIResponse<AccountUnifyUserInfoResponse> selectUnifyUserInfo(@Validated @RequestBody APIRequest<AccountUnifyUserInfoRequest> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectUnifyUserInfo(request.getBody()));
    }

    @Override
    public APIResponse<UserParentOrRootRelationShipByUserIdResp> userParentOrRootRelationShipByUserId(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(iUserInfo.userParentOrRootRelationShipByUserId(request.getBody()));

    }

    @Override
    public APIResponse<Boolean> selectIfHasAgentUser(@Validated @RequestBody APIRequest<SelectIfHasAgenUserRequest> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectIfHasAgentUser(request.getBody()));
    }

    @Override
    public APIResponse<Long> selectParentBrokerUserId(@Validated @RequestBody APIRequest<Long> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectParentBrokerUserId(request.getBody()));
    }

    @Override
    public APIResponse<Map<String,Object>> getAccountStatusForWapi(@Validated @RequestBody APIRequest<Long> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.getAccountStatusForWapi(request.getBody()));
    }

    @Override
    public APIResponse<SelectUserRiskMessage> selectUserMessgeForRisk(@Validated @RequestBody APIRequest<Long> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectUserMessgeForRisk(request.getBody()));
    }

    @Override
    public APIResponse<SelectUserRegisterTimeResponse> selectRegisterTimeByUserId(@Validated @RequestBody APIRequest<Long> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectRegisterTimeByUserId(request.getBody()));
    }

    @Override
    public APIResponse<List<SelectRootUserIdsResponse>> selectRootUserIds(@Validated @RequestBody APIRequest<SelectRootUserRequest> request) throws Exception{
        return APIResponse.getOKJsonResult(iUserInfo.selectRootUserIds(request.getBody()));
    }
}
