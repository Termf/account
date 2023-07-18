package com.binance.account.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.binance.account.vo.user.UserGroupVo;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/userInfo")
@Api(value = "用户信息")
public interface UserInfoApi {

    @ApiOperation("根据userId更新用户信息(同步修改子账户)")
    @PostMapping("/updateUserInfoByUserId")
    APIResponse<Integer> updateUserInfoByUserId(@RequestBody APIRequest<UpdateUserInfoByUserIdRequest> request)
            throws Exception;

    @ApiOperation("修改用户返佣比例(同步修改子账户)")
    @PostMapping("/updateAgentRewardRatio")
    APIResponse<Integer> updateAgentRewardRatio(@RequestBody APIRequest<UpdateAgentRewardRatioRequest> request)
            throws Exception;

    @ApiOperation("设置用户交易费(同步修改子账户)")
    @PostMapping("/commission/setting")
    APIResponse<Integer> setCommission(@Validated @RequestBody APIRequest<SetCommissionRequest> request)
            throws Exception;

    @ApiOperation("设置用户交易级别(同步修改子账户)")
    @PostMapping("/tradeLevel/setting")
    APIResponse<Integer> setTradeLevel(@Validated @RequestBody APIRequest<SetTradeLevelRequest> request)
            throws Exception;

    @ApiOperation("设置用户是否自动更新交易级别")
    @PostMapping("/tradeStatus/setting")
    APIResponse<Integer> setTradeAutoStatus(@Validated @RequestBody APIRequest<SetTradeAutoStatus> request)
            throws Exception;

    @ApiOperation("设置用户TradeLevel和手续费(同步修改子账户)")
    @PostMapping("/levelAndCommission/setting")
    APIResponse<Integer> setTradeLevelAndCommissionRequest(
            @Validated @RequestBody APIRequest<SetTradeLevelAndCommissionRequest> request) throws Exception;

    @ApiOperation("修复pnk的TradingAccount数据")
    @PostMapping("/updatePnkTradingAccount")
    APIResponse<Integer> updatePnkTradingAccount() throws Exception;

    @ApiOperation("设置用户的默认配置")
    @PostMapping("/saveUserConfig")
    APIResponse<Integer> saveUserConfig(@Validated @RequestBody APIRequest<SetUserConfigRequest> request)
            throws Exception;

    @ApiOperation("根据用户信息查询用户的默认配置")
    @PostMapping("/selectUserConfig")
    APIResponse<List<UserConfigResponse>> selectUserConfig(
            @Validated @RequestBody APIRequest<SelectUserConfigRequest> request) throws Exception;

    @ApiOperation("获取用户返佣列表")
    @PostMapping("/getUserAgentList")
    APIResponse<GetUserAgentRewardResponse> getUserAgentList(
            @Validated @RequestBody APIRequest<AgentRewardRequest> request) throws Exception;

    @ApiOperation("修改推荐人(同步修改子账户)")
    @PostMapping("/updateAgentId")
    APIResponse<Integer> updateAgentId(@Validated @RequestBody APIRequest<UserAgentIdRequest> request) throws Exception;

    @ApiOperation("修改Future推荐人(同步修改子账户)")
    @PostMapping("/updateFutureAgentId")
    public APIResponse<Integer> updateFutureAgentId(@Validated @RequestBody APIRequest<UserFutureAgentIdRequest> request) throws Exception;

    @ApiOperation("根据Future查询子账户对应的Future(同步修改子账户)")
    @PostMapping("/selectSubFutureUserIds")
    public APIResponse<List<Long>> selectSubFutureUserIds(@Validated @RequestBody APIRequest<SelectSubFutureUserIdsRequest> request) throws Exception;

    @ApiOperation("修改用户返佣比例--新版(同步修改子账户)")
    @PostMapping("/updateUserAgentRatio")
    APIResponse<Integer> updateUserAgentRatio(@Validated @RequestBody APIRequest<UpdateUserAgentRewardRequest> request)
            throws Exception;

    @ApiOperation("批量修改用户返佣比例(同步修改子账户)")
    @PostMapping("/batchUpdateAgentId")
    APIResponse<BatchUpdateAgentResponse> batchUpdateAgentId(
            @Validated @RequestBody APIRequest<UpdateUserAgentRewardListRequest> request) throws Exception;

    @ApiOperation("获取用户返佣审核列表")
    @PostMapping("/getUserAgentRewardList")
    APIResponse<GetUserAgentRewardResponse> getUserAgentRewardList(
            @Validated @RequestBody APIRequest<AgentRewardAuditRequest> request) throws Exception;

    @ApiOperation("根据主键id修改返佣审核状态(同步修改子账户)")
    @PostMapping("/updateAgentStatus")
    APIResponse<ResultDateResponse> updateAgentStatus(@Validated @RequestBody APIRequest<AgentStatusRequest> request)
            throws Exception;

    @ApiOperation("根据批次号修改返佣审核状态(同步修改子账户)")
    @PostMapping("/updateAgentStatusByBatchId")
    APIResponse<ResultDateResponse> updateAgentStatusByBatchId(
            @Validated @RequestBody APIRequest<AgentStatusByBatchIdRequest> request) throws Exception;

    @ApiOperation("根据")
    @PostMapping("/selectAgentUserIdByBatchIds")
    APIResponse<List<Long>> selectAgentUserIdByBatchIds(@Validated @RequestBody APIRequest<AgentStatusByBatchIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户id查用户返佣比例")
    @PostMapping("/selectUserInfoRewardRatio")
    APIResponse<UserInfoRewardRatioResponse> selectUserInfoRewardRatio(
            @Validated @RequestBody APIRequest<BaseDetailRequest> request) throws Exception;

    @ApiOperation("根据用户id查用UserInfo")
    @PostMapping("/query/user-id")
    APIResponse<UserInfoVo> getUserInfoByUserId(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    @ApiOperation("根据用户ids查用UserInfos,内部使用最多500条")
    @PostMapping("/getUserInfosByUserIds")
    APIResponse<List<UserInfoVo>> getUserInfosByUserIds(@Validated @RequestBody APIRequest<List<Long>> request) throws Exception;

    @ApiOperation("根据用户id查用户手续费")
    @PostMapping("/getUserCommission")
    APIResponse<UserCommissionResponse> getUserCommission(@Validated @RequestBody APIRequest<IdLongRequest> request)
            throws Exception;

    @ApiOperation("根据用户id查用户提现额度--临时接口后续要迁移到asset-service")
    @PostMapping("/getUserLevelWithdreaw")
    APIResponse<List<BigDecimal>> getUserLevelWithdreaw(APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("修改用户提现限额")
    @PostMapping("/daily/withdraw/cap/update")
    APIResponse<Integer> updateUserDailyWithdrawCap(@Validated @RequestBody APIRequest<UpdateDailyWithdrawCapRequest> request) throws Exception;

    @ApiOperation("查询用户提现限额")
    @PostMapping("/daily/withdraw/cap/query")
    APIResponse<BigDecimal> getUserDailyWithdrawCap(@Validated @RequestBody APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation("修改用户法币提现限额")
    @PostMapping("/daily/fiat/withdraw/cap/update")
    APIResponse<Integer> updateUserDailyFiatWithdrawCap(@Validated @RequestBody APIRequest<UpdateDailyFiatWithdrawCapRequest> request) throws Exception;

    @ApiOperation("所有用户账户集合列表（仅支持美国站使用）")
    @PostMapping("/us/job/allUserGroup")
    APIResponse<List<UserGroupVo>> allUserGroup(@Validated @RequestBody APIRequest<APIRequest.VoidBody> request) throws Exception;

    @ApiOperation("更新用户的手续费率（仅支持美国站使用）")
    @PostMapping("/us/job/batchUpdateUserTradeLevelAndCommission")
    APIResponse<List<Long>> batchUpdateUserTradeLevelAndCommission(@Validated @RequestBody APIRequest<List<SetTradeLevelAndCommissionRequest>> request) throws Exception;


    @ApiOperation("查询个人基本信息-用于adminui")
    @PostMapping("/selectUnifyUserInfo")
    APIResponse<AccountUnifyUserInfoResponse> selectUnifyUserInfo(@Validated @RequestBody APIRequest<AccountUnifyUserInfoRequest> request) throws Exception;


    @ApiOperation("根据用户id查用主账户或者母账户信息")
    @PostMapping("/query/userParentOrRootRelationShipByUserId")
    APIResponse<UserParentOrRootRelationShipByUserIdResp> userParentOrRootRelationShipByUserId(@Validated @RequestBody APIRequest<UserIdRequest> request)
            throws Exception;


    @ApiOperation("查询一段时间内是否有注册的用户-非通用")
    @PostMapping("/selectIfHasAgentUser")
    APIResponse<Boolean> selectIfHasAgentUser(@Validated @RequestBody APIRequest<SelectIfHasAgenUserRequest> request) throws Exception;

    @ApiOperation("根据futureUserId查询主账户对应的parent.brokerUserId-非通用")
    @PostMapping("/selectParentBrokerUserId")
    APIResponse<Long> selectParentBrokerUserId(@Validated @RequestBody APIRequest<Long> request) throws Exception;

    @ApiOperation("wapi迁移接口-非通用")
    @PostMapping("/getAccountStatusForWapi")
    APIResponse<Map<String,Object>> getAccountStatusForWapi(@Validated @RequestBody APIRequest<Long> request) throws Exception;

    @ApiOperation("根据userID查询风控信息")
    @PostMapping("/selectUserMessgeForRisk")
    APIResponse<SelectUserRiskMessage> selectUserMessgeForRisk(@Validated @RequestBody APIRequest<Long> request) throws Exception;


    @ApiOperation("根据userId查询出用户创建各种账户的时间以及")
    @PostMapping("/selectRegisterTimeByUserId")
    APIResponse<SelectUserRegisterTimeResponse> selectRegisterTimeByUserId(@Validated @RequestBody APIRequest<Long> request) throws Exception;

    @ApiOperation("根据非rootUserId查询rootUserIds")
    @PostMapping("/selectRootUserIds")
    APIResponse<List<SelectRootUserIdsResponse>> selectRootUserIds(@Validated @RequestBody APIRequest<SelectRootUserRequest> request) throws Exception;

}
