package com.binance.account.service.user;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.subuser.request.SelectIfHasAgenUserRequest;
import com.binance.account.vo.user.UserGroupVo;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.request.AccountUnifyUserInfoRequest;
import com.binance.account.vo.user.request.AgentRewardAuditRequest;
import com.binance.account.vo.user.request.AgentRewardRequest;
import com.binance.account.vo.user.request.AgentStatusByBatchIdRequest;
import com.binance.account.vo.user.request.AgentStatusRequest;
import com.binance.account.vo.user.request.AuditAgentStatusByBatchIdRequest;
import com.binance.account.vo.user.request.BaseDetailRequest;
import com.binance.account.vo.user.request.SelectRootUserRequest;
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
import com.binance.account.vo.user.response.AccountUnifyUserInfoResponse;
import com.binance.account.vo.user.response.AuditAgentStatusByBatchIdResponse;
import com.binance.account.vo.user.response.BatchUpdateAgentResponse;
import com.binance.account.vo.user.response.GetUserAgentRewardResponse;
import com.binance.account.vo.user.response.ResultDateResponse;
import com.binance.account.vo.user.response.SelectRootUserIdsResponse;
import com.binance.account.vo.user.response.SelectUserRegisterTimeResponse;
import com.binance.account.vo.user.response.SelectUserRiskMessage;
import com.binance.account.vo.user.response.UserCommissionResponse;
import com.binance.account.vo.user.response.UserConfigResponse;
import com.binance.account.vo.user.response.UserInfoRewardRatioResponse;
import com.binance.account.vo.user.response.UserParentOrRootRelationShipByUserIdResp;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IUserInfo {

    /**
     * 根据用户Id更新用户信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Integer> updateUserInfoByUserId(APIRequest<UpdateUserInfoByUserIdRequest> request) throws Exception;

    APIResponse<Integer> updateAgentRewardRatio(APIRequest<UpdateAgentRewardRatioRequest> request) throws Exception;

    APIResponse<Integer> setCommission(APIRequest<SetCommissionRequest> request) throws Exception;

    APIResponse<Integer> setTradeLevel(APIRequest<SetTradeLevelRequest> request) throws Exception;

    APIResponse<Integer> setTradeAutoStatus(APIRequest<SetTradeAutoStatus> request) throws Exception;

    APIResponse<Integer> setTradeLevelAndCommissionRequest(APIRequest<SetTradeLevelAndCommissionRequest> request)
            throws Exception;

    APIResponse<Integer> updatePnkTradingAccount();

    /**
     * 设置用户默认配置
     *
     * @param request
     * @return
     */
    APIResponse<Integer> saveUserConfig(APIRequest<SetUserConfigRequest> request);

    /**
     * 查询用户默认配置
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<List<UserConfigResponse>> selectUserConfig(APIRequest<SelectUserConfigRequest> request)
            throws Exception;

    /**
     * 获取用户分佣比例列表
     *
     * @param request
     * @return
     */
    APIResponse<GetUserAgentRewardResponse> getUserAgentList(APIRequest<AgentRewardRequest> request) throws Exception;

    /**
     * 修改推荐人
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateAgentId(APIRequest<UserAgentIdRequest> request);

    /**
     * 修改用户分佣比例--新版
     *
     * @param request
     * @return
     */
    APIResponse<Integer> updateUserAgentRatio(APIRequest<UpdateUserAgentRewardRequest> request);

    /**
     * 批量修改用户分佣比例
     *
     * @param request
     * @return
     */
    APIResponse<BatchUpdateAgentResponse> batchUpdateAgentId(APIRequest<UpdateUserAgentRewardListRequest> request);

    /**
     * 获取用户分佣比例审核列表
     *
     * @param request
     * @return
     */
    APIResponse<GetUserAgentRewardResponse> getUserAgentRewardList(APIRequest<AgentRewardAuditRequest> request);

    /**
     * 修改返佣审核状态
     *
     * @param request
     * @return
     */
    APIResponse<ResultDateResponse> updateAgentStatus(APIRequest<AgentStatusRequest> request);

    /**
     * 根据批次号修改返佣审核状态
     *
     * @param request
     * @return
     */
    APIResponse<ResultDateResponse> updateAgentStatusByBatchId(APIRequest<AgentStatusByBatchIdRequest> request);

    /**
     * 根据批次号修改返佣审核状态(并返回对应用户ID)
     */
    APIResponse<AuditAgentStatusByBatchIdResponse> auditAgentStatusByBatchId(APIRequest<AuditAgentStatusByBatchIdRequest> request) throws Exception;

    /**
     * 查用户批次号
     *
     * @param request
     * @return
     */
    APIResponse<UserInfoRewardRatioResponse> selectUserInfoRewardRatio(APIRequest<BaseDetailRequest> request);

    /**
     * 根据用户id查用UserInfo
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<UserInfoVo> getUserInfoByUserId(APIRequest<UserIdRequest> request) throws Exception;

    UserInfoVo getUserInfoByUserId(Long userId) throws Exception;

    /**
     * 查用户手续费
     * @param request
     * @return
     */
    APIResponse<UserCommissionResponse> getUserCommission(APIRequest<IdLongRequest> request);

    /**
     * 查询用户提现额度
     *
     * @param request
     * @return
     */
    APIResponse<List<BigDecimal>> getUserLevelWithdreaw(APIRequest<UserIdRequest> request);

    /**
     * 修改用户提现限额
     *
     * @param request
     * @return
     * @throws Exception
     */
    Integer updateUserDailyWithdrawCap(UpdateDailyWithdrawCapRequest request) throws Exception;

    /**
     * 查询用户提现限额
     *
     * @param request
     * @return
     * @throws Exception
     */
    BigDecimal getUserDailyWithdrawCap(UserIdRequest request) throws Exception;

    /**
     * 修改用户法币提现限额
     *
     * @param request
     * @return
     * @throws Exception
     */
    Integer updateUserDailyFiatWithdrawCap(UpdateDailyFiatWithdrawCapRequest request) throws Exception;

    String getUserLastLoginLanguage(Long userId)
            throws Exception;

    List<UserGroupVo> allUserGroup() throws Exception;


    List<Long> batchUpdateUserTradeLevelAndCommission(List<SetTradeLevelAndCommissionRequest> request);

    AccountUnifyUserInfoResponse selectUnifyUserInfo(AccountUnifyUserInfoRequest body) throws Exception;

    APIResponse<List<Long>> selectAgentUserIdByBatchIds(APIRequest<AgentStatusByBatchIdRequest> request) throws Exception;


    UserParentOrRootRelationShipByUserIdResp userParentOrRootRelationShipByUserId(UserIdRequest request) throws Exception;


    APIResponse<List<UserInfoVo>> getUserInfosByUserIds(APIRequest<List<Long>> request) throws Exception;

    APIResponse<Integer> updateFutureAgentId(APIRequest<UserFutureAgentIdRequest> request) throws Exception;

    APIResponse<List<Long>> selectSubFutureUserIds(APIRequest<SelectSubFutureUserIdsRequest> request)throws Exception;

    Boolean selectIfHasAgentUser(SelectIfHasAgenUserRequest body)throws Exception;

    Long selectParentBrokerUserId(Long body)throws Exception;

    Map<String,Object> getAccountStatusForWapi(Long userId);

    SelectUserRiskMessage selectUserMessgeForRisk(Long userId)throws Exception;

    SelectUserRegisterTimeResponse selectRegisterTimeByUserId(Long body)throws Exception;

    List<SelectRootUserIdsResponse> selectRootUserIds(SelectRootUserRequest body)throws Exception;
}
