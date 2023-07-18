package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.SecurityLog;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.constants.enums.AccountTypeEnum;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.margin.IsolatedMarginUserBinding;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.RelationUserInfo;
import com.binance.account.data.entity.user.RootUserIndex;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserAgentReward;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.device.UserDeviceMapper;
import com.binance.account.data.mapper.margin.IsolatedMarginUserBindingMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.*;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.integration.capital.CapitalClient;
import com.binance.account.integration.risk.RiskSecurityApiClient;
import com.binance.account.utils.InboxUtils;
import com.binance.account.vo.subuser.request.SelectIfHasAgenUserRequest;
import com.binance.account.vo.user.UserGroupVo;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import com.binance.capital.vo.withdraw.response.GetWithdrawMessageResponse;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.WebUtils;
import com.binance.risk.vo.RiskSecurityVo;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import org.apache.commons.collections4.CollectionUtils;
import com.google.common.collect.Maps;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.SecurityLog;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.RootUserIndexMapper;
import com.binance.account.data.mapper.user.UserAgentRewardMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.integration.capital.CapitalClient;
import com.binance.account.integration.futureservice.CommissionApiClient;
import com.binance.account.service.async.UserInfoAsyncTask;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.service.user.IUserInfo;
import com.binance.account.service.userconfig.IUserConfig;
import com.binance.account.utils.InboxUtils;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.SelectIfHasAgenUserRequest;
import com.binance.account.vo.user.UserAgentRewardVo;
import com.binance.account.vo.user.UserGroupVo;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
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
import com.binance.capital.vo.withdraw.response.GetWithdrawMessageResponse;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.data.account.OldSecurityPolicyMapper;
import com.binance.master.old.data.account.OldUserDataMapper;
import com.binance.master.old.data.account.OldUserMapper;
import com.binance.master.old.models.account.OldSecurityPolicy;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.CouplingCalculationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.esotericsoftware.minlog.Log;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserInfoBusiness implements IUserInfo {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private AccountApi accountApi;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private IMsgNotification iMsgNotification;
    @Resource
    private OldUserMapper oldUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserAgentRewardMapper userAgentRewardMapper;
    @Resource
    private IUserConfig iUserConfig;
    @Resource
    private OldSecurityPolicyMapper oldSecurityPolicyMapper;
    @Resource
    private OldUserDataMapper oldUserDataMapper;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Autowired
    protected SubUserBindingMapper subUserBindingMapper;
    @Autowired
    private UserInfoAsyncTask userInfoAsyncTask;
    @Autowired
    private ISubUserAdmin subUserAdminBusiness;
    @Autowired
    private InboxMessageTextApi inboxMessageTextApi;
    @Autowired
    private UserSecurityMapper userSecurityMapper;
    @Autowired
    private UserSecurityLogMapper userSecurityLogMapper;
    @Autowired
    private UserAgentLogMapper userAgentLogMapper;
    @Autowired
    private CapitalClient capitalClient;
    @Autowired
    private IsolatedMarginUserBindingMapper isolatedMarginUserBindingMapper;
    @Autowired
    private RiskSecurityApiClient riskSecurityApiClient;
    @Resource
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private CountryMapper countryMapper;
    @Autowired
    private UserDeviceMapper userDeviceMapper;
    @Autowired
    private UserOperationLogMapper userOperationLogMapper;
    @Autowired
    private RootUserIndexMapper rootUserIndexMapper;
    @Autowired
    private CommissionApiClient commissionApiClient;


    @Value("${autoEnable.subUserFunction.tradeLevel:false}")
    private boolean autoEnableSubUserFunctionDueToTradeLevel;
    public static final String SUCCESS = "success";
    public static final String MSG = "msg";


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateUserInfoByUserId(APIRequest<UpdateUserInfoByUserIdRequest> request) {
        final UpdateUserInfoByUserIdRequest requestBody = request.getBody();

        final UserInfoVo requestUserInfo = requestBody.getUserInfo();

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(requestUserInfo, userInfo);

        // 更新用户UserInfo
        final Integer rows = this.userInfoMapper.updateByPrimaryKeySelective(userInfo);

        // 若为母账户,则更新子账户UserInfo
        try {
            User user = userCommonBusiness.checkAndGetUserById(requestBody.getUserId());
            if (userCommonBusiness.isParentUser(user.getStatus())) {
                List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(user.getUserId());
                if (CollectionUtils.isNotEmpty(subUserIds)) {
                    int total = subUserIds.size();
                    int success = 0;
                    for (Long subUserId : subUserIds) {
                        try {
                            UserInfo subUserInfo = userInfoMapper.selectByPrimaryKey(subUserId);
                            if (null != subUserInfo) {
                                UserInfo updatedUserInfo = new UserInfo();
                                updatedUserInfo.setUserId(subUserId);
                                updatedUserInfo.setTradeLevel(requestUserInfo.getTradeLevel());
                                updatedUserInfo.setBuyerCommission(requestUserInfo.getBuyerCommission());
                                updatedUserInfo.setSellerCommission(requestUserInfo.getSellerCommission());
                                updatedUserInfo.setTakerCommission(requestUserInfo.getTakerCommission());
                                updatedUserInfo.setMakerCommission(requestUserInfo.getMakerCommission());
                                updatedUserInfo.setAgentId(requestUserInfo.getAgentId());
                                updatedUserInfo.setAgentRewardRatio(requestUserInfo.getAgentRewardRatio());
                                int subResult = userInfoMapper.updateByPrimaryKeySelective(updatedUserInfo);
                                if (1 == subResult) {
                                    success++;
                                }
                            }
                        } catch (Exception e) {
                            log.error(String.format("updateSubUsersAgentRatio error, subUserId:%s.", subUserId), e);
                        }
                    }
                    log.info("updateUserInfoByUserId for subUsers, parentUserId:{}, total:{}, success:{}",
                            requestBody.getUserId(), total, success);
                }
            }

            if (userCommonBusiness.isExistMarginAccount(user.getUserId())) {
                log.info("updateUserInfoByUserId for margin,rootUserId:{}", requestBody.getUserId());
                UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
                UserInfo marginUserInfo = userInfoMapper.selectByPrimaryKey(rootUserInfo.getMarginUserId());
                if (null != marginUserInfo) {
                    marginUserInfo.setTradeLevel(requestUserInfo.getTradeLevel());
                    marginUserInfo.setBuyerCommission(requestUserInfo.getBuyerCommission());
                    marginUserInfo.setSellerCommission(requestUserInfo.getSellerCommission());
                    marginUserInfo.setTakerCommission(requestUserInfo.getTakerCommission());
                    marginUserInfo.setMakerCommission(requestUserInfo.getMakerCommission());
                    int result = userInfoMapper.updateByPrimaryKeySelective(marginUserInfo);
                    log.info("updateUserInfoByUserId for margin, rootUserId:{}, marginUserId:{}, result:{}",
                            requestBody.getUserId(), rootUserInfo.getMarginUserId(), result);
                }
            }
        } catch (Exception e) {
            log.error("updateUserInfoByUserId error", e);
        }

        return APIResponse.getOKJsonResult(rows);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateAgentId(APIRequest<UserAgentIdRequest> request) {
        final UserAgentIdRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        final Long agentId = requestBody.getAgentId();

        User user = userCommonBusiness.checkAndGetUserById(userId);
        if (!com.binance.account.constants.enums.UserTypeEnum.NORMAL.name().equals(com.binance.account.constants.enums.UserTypeEnum.getAccountTypeName(user.getStatus()))
                || BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        if (userId.equals(agentId) &&  !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        User agentUser = userCommonBusiness.checkAndGetUserById(agentId);
        if (!com.binance.account.constants.enums.UserTypeEnum.NORMAL.name().equals(com.binance.account.constants.enums.UserTypeEnum.getAccountTypeName(agentUser.getStatus()))){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 更新用户推荐人
        int result = updateAgentId(userId, agentId,true);

        // 若为母账户,则更新子账户推荐人
        if (userCommonBusiness.isParentUser(user.getStatus())) {
            userInfoAsyncTask.updateSubUserAgentId(userId, agentId);
        }

        return APIResponse.getOKJsonResult(result);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateFutureAgentId(APIRequest<UserFutureAgentIdRequest> request) {
        final UserFutureAgentIdRequest requestBody = request.getBody();
        Long futureUserId = requestBody.getFutureUserId();
        final Long futureAgentId = requestBody.getFutureAgentId();
        UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
        if (rootUserInfo == null){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 更新用户推荐人
        int result = updateFutureAgentId(futureUserId, futureAgentId);
        User user = userCommonBusiness.checkAndGetUserById(rootUserInfo.getUserId());
        // 若为母账户,则更新子账户推荐人
        if (userCommonBusiness.isParentUser(user.getStatus())) {
            updateSubUserFutureAgentId(rootUserInfo.getUserId(), futureAgentId);
        }
        return APIResponse.getOKJsonResult(result);
    }

    public APIResponse<List<Long>> selectSubFutureUserIds(APIRequest<SelectSubFutureUserIdsRequest> request){
        UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(request.getBody().getFutureUserId());
        User user = userCommonBusiness.checkAndGetUserById(rootUserInfo.getUserId());
        // 若为母账户,则更新子账户推荐人
        if (!userCommonBusiness.isParentUser(user.getStatus())) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(rootUserInfo.getUserId());
        if (CollectionUtils.isEmpty(subUserIds)){
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<Long> subFutureUserIds = new ArrayList<>(subUserIds.size());
        for (Long subUserId:subUserIds){
            UserInfo subUserInfo = userInfoMapper.selectByPrimaryKey(subUserId);
            if (subUserInfo != null && subUserInfo.getFutureUserId() != null){
                subFutureUserIds.add(subUserInfo.getFutureUserId());
            }
        }
        return APIResponse.getOKJsonResult(subFutureUserIds);
    }

    public void updateSubUserFutureAgentId(final Long parentUserId, final Long futureAgentId) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    UserInfo subRootUserInfo = userInfoMapper.selectByPrimaryKey(subUserId);
                    if (subRootUserInfo != null && subRootUserInfo.getFutureUserId() != null){
                        UserInfo userInfo = new UserInfo();
                        userInfo.setFutureAgentId(futureAgentId);
                        userInfo.setUserId(subRootUserInfo.getFutureUserId());
                        int subResult = userInfoMapper.updateByPrimaryKeySelective(userInfo);
                        if (1 == subResult) {
                            success++;
                        }
                    }
                } catch (Exception e) {
                    log.error(String.format("updateSubUserFutureAgentId error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("updateSubUserFutureAgentId for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId, total,
                    success);
        }
    }

    public int updateFutureAgentId(final Long futureUserId, final Long futureAgentId) {
        UserInfo futureUserInfo = this.userInfoMapper.selectByPrimaryKey(futureUserId);
        if (futureUserInfo == null) {// 该用户不存在
            return 0;
        }
        // 2.update user_info的futureagentId
        UserInfo userInfo = new UserInfo();
        userInfo.setFutureAgentId(futureAgentId);
        userInfo.setUserId(futureUserId);
        return this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }


    public int updateAgentId(final Long userId, final Long agentId ,boolean isUpdateAll) {
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui == null) {// 该用户不存在
            return 0;
        }
        // 1.update user_agent_reward的agentId
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, userId);
        paramMap.put("agentId", agentId);
        paramMap.put(UserConst.UPDATE_TIME, new Date());// 修改时间
        this.userAgentRewardMapper.updateAgentRewardByUserId(paramMap);
        if (isUpdateAll){
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            if (userInfo.getMarginUserId() != null){
                UserInfo marginUserInfo = new UserInfo();
                marginUserInfo.setAgentId(agentId);
                marginUserInfo.setUserId(userInfo.getMarginUserId());
                this.userInfoMapper.updateByPrimaryKeySelective(marginUserInfo);
            }
            if (userInfo.getFutureUserId() != null){
                UserInfo futureAgentUserInfo = userInfoMapper.selectByPrimaryKey(agentId);
                if (futureAgentUserInfo.getFutureUserId() != null){
                    UserInfo futureUserInfo = new UserInfo();
                    futureUserInfo.setFutureAgentId(futureAgentUserInfo.getFutureUserId());
                    futureUserInfo.setUserId(userInfo.getFutureUserId());
                    this.userInfoMapper.updateByPrimaryKeySelective(futureUserInfo);
                    commissionApiClient.updateFutureAgent(userInfo.getFutureUserId(),futureAgentUserInfo.getFutureUserId());
                }
            }

            List<Long> isoLatedMarginUserIds = isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(userId);
            for (Long isoLatedMargin:isoLatedMarginUserIds){
                UserInfo isoLatedMarginUserInfo = new UserInfo();
                isoLatedMarginUserInfo.setAgentId(agentId);
                isoLatedMarginUserInfo.setUserId(isoLatedMargin);
                this.userInfoMapper.updateByPrimaryKeySelective(isoLatedMarginUserInfo);
            }
        }
        // 2.update user_info的agentId
        UserInfo updateuserInfo = new UserInfo();
        updateuserInfo.setAgentId(agentId);
        updateuserInfo.setUserId(userId);
        return this.userInfoMapper.updateByPrimaryKeySelective(updateuserInfo);
    }

    @SecurityLog(name = "修改返佣比例", operateType = Constant.SECURITY_OPERATE_TYPE_UPDATE_AGENT_REWARD_RATIO,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateAgentRewardRatio(APIRequest<UpdateAgentRewardRatioRequest> request) {
        final UpdateAgentRewardRatioRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        final BigDecimal agentRewardRatio = requestBody.getAgentRewardRatio();

        User user = userCommonBusiness.checkAndGetUserById(userId);

        // 更新用户返佣比例
        int result = updateAgentRewardRatio(userId, agentRewardRatio);

        // 若为母账户,则更新子账户返佣比例
        if (userCommonBusiness.isParentUser(user.getStatus())) {
            userInfoAsyncTask.updateSubUserAgentRewardRatio(userId, agentRewardRatio);
        }

        return APIResponse.getOKJsonResult(result);
    }

    public int updateAgentRewardRatio(final Long userId, final BigDecimal agentRewardRatio) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setAgentRewardRatio(agentRewardRatio);
        this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return 1;
    }

    /**
     * 修改user_info的返佣比例
     *
     * @param oldAgentRewardRatio:旧返佣比例
     * @param newAgentRewardRatio:新返佣比例
     */
    private int updateUserInfo(BigDecimal oldAgentRewardRatio, BigDecimal newAgentRewardRatio, Long userId) {
        if (!oldAgentRewardRatio.equals(newAgentRewardRatio)) {// 不一样就修改
            UserInfo userInfo = new UserInfo();
            userInfo.setAgentRewardRatio(newAgentRewardRatio);
            userInfo.setUserId(userId);
            return this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
        }
        return 0;
    }

    @Override
    public APIResponse<Integer> setCommission(APIRequest<SetCommissionRequest> request) {

        final SetCommissionRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();

        User user = userCommonBusiness.checkAndGetUserById(userId);

        BigDecimal buyerCommission = requestBody.getBuyerCommission();
        BigDecimal sellerCommission = requestBody.getSellerCommission();
        BigDecimal takerCommission = requestBody.getTakerCommission();
        BigDecimal makerCommission = requestBody.getMakerCommission();

        // 更新用户手续费
        int result = setCommission(userId, buyerCommission, sellerCommission, takerCommission, makerCommission,
                requestBody.getModifyReason(), requestBody.getExpectedRestoreTime());
        log.info("setCommission done, userId:{}, result:{}", userId, result);

        // 若该账户为母账户,则更新子账户手续费
        if (userCommonBusiness.isParentUser(user.getStatus()) && !userCommonBusiness.isBrokerParentUser(user.getStatus()) ) {
            userInfoAsyncTask.setSubUsersCommission(userId, buyerCommission, sellerCommission, takerCommission,
                    makerCommission, requestBody.getModifyReason(), requestBody.getExpectedRestoreTime());
        }
        // 若该账户有margin账户,则更新margin账户手续费
        if (userCommonBusiness.isExistMarginAccount(userId)) {
            userInfoAsyncTask.setMarginUserCommission(userId, buyerCommission, sellerCommission, takerCommission,
                    makerCommission, requestBody.getModifyReason(), requestBody.getExpectedRestoreTime());
        }


        // 若该账户有逐仓margin账户,则更新逐仓margi账户手续费
        if (userCommonBusiness.isExistIsolatedMarginAccount(userId)) {
            userInfoAsyncTask.setIsolatedMarginUserCommission(userId, buyerCommission, sellerCommission, takerCommission,
                    makerCommission, requestBody.getModifyReason(), requestBody.getExpectedRestoreTime());
        }

        return APIResponse.getOKJsonResult(result);
    }



    public int setCommission(final Long userId, final BigDecimal buyerCommission, final BigDecimal sellerCommission,
            final BigDecimal takerCommission, final BigDecimal makerCommission, final String modifyReason,
            final String expectedRestoreTime) {

        int num = 0;
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo) {

            if (null != userInfo.getTradingAccount()) {
                TradingAccountDetails tradingAccountDetails= accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
                log.info("getDetailsByTradingAccountId userId={},tradingAccountDetails={},userInfo={}",userId,JsonUtils.toJsonNotNullKey(tradingAccountDetails),
                        JsonUtils.toJsonNotNullKey(userInfo));
                boolean isEqual=tradingAccountDetails.getBuyerCommission().longValue()==CouplingCalculationUtils.feeLong(buyerCommission)&&
                        tradingAccountDetails.getSellerCommission().longValue()==CouplingCalculationUtils.feeLong(sellerCommission)&&
                        tradingAccountDetails.getTakerCommission().longValue()==CouplingCalculationUtils.feeLong(takerCommission)&&
                        tradingAccountDetails.getMakerCommission().longValue()==CouplingCalculationUtils.feeLong(makerCommission);
                if(!isEqual){
                    accountApi.setCommission(userInfo.getTradingAccount(),
                            CouplingCalculationUtils.feeLong(buyerCommission),
                            CouplingCalculationUtils.feeLong(sellerCommission),
                            CouplingCalculationUtils.feeLong(takerCommission),
                            CouplingCalculationUtils.feeLong(makerCommission));
                }
            } else {
                UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
                User user = userMapper.queryByEmail(userIndex.getEmail());
                if (BitUtils.isTrue(user.getStatus(), Constant.USER_ACTIVE)) {
                    log.error("setCommission user activated, but tradingAccount is null, userId:{}", userId);
                } else {
                    log.warn("setCommission user not activated and tradingAccount is null, userId:{}", userId);
                }
            }

            // 读写延迟
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUserId(userId);
            updateUserInfo.setBuyerCommission(buyerCommission);
            updateUserInfo.setSellerCommission(sellerCommission);
            updateUserInfo.setTakerCommission(takerCommission);
            updateUserInfo.setMakerCommission(makerCommission);
            num = userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);

        } else {
            log.error("userInfo is null, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        try {
            // 临时代码
            Map<String, Object> dataMsg = new HashMap<>();
            dataMsg.put(UserConst.USER_ID, userId);
            dataMsg.put("buyerCommission", buyerCommission);
            dataMsg.put("sellerCommission", sellerCommission);
            dataMsg.put("takerCommission", takerCommission);
            dataMsg.put("makerCommission", makerCommission);
            dataMsg.put("modifyReason", modifyReason);
            dataMsg.put("expectedRestoreTime", expectedRestoreTime);
            MsgNotification msg =
                    new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.SET_COMMISSION, dataMsg);
            log.info("iMsgNotification setCommission:{}", JSON.toJSONString(msg));
            this.iMsgNotification.send(msg);
        } catch (Exception e) {
            log.error("iMsgNotification.send failed:", e);
        }
        return num;
    }

    @Override
    public APIResponse<Integer> setTradeLevel(APIRequest<SetTradeLevelRequest> request) {
        final SetTradeLevelRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        final Integer tradeLevel = requestBody.getTradeLevel();

        final User user = userCommonBusiness.checkAndGetUserById(userId);

        // 更新用户TradeLevel
        int result = setTradeLevel(userId, tradeLevel);
        log.info("setTradeLevel done, userId:{}, result:{}", userId, result);

        // 该账户若为母账户,则异步更新子账户TradeLevel
        if (userCommonBusiness.isParentUser(user.getStatus()) && !userCommonBusiness.isBrokerParentUser(user.getStatus())) {
            userInfoAsyncTask.setSubUserTradeLevel(userId, tradeLevel);
        }

        // 该账户有margin账户,则异步更新其margin账户的TradeLevel
        if (userCommonBusiness.isExistMarginAccount(user.getUserId())) {
            userInfoAsyncTask.setMarginUserTradeLevel(userId, tradeLevel);
        }

        // 该账户有future账户,则异步更新其future账户的TradeLevel
        if (userCommonBusiness.isExistFutureAccount(user.getUserId())) {
            userInfoAsyncTask.setFutureUserTradeLevel(userId, tradeLevel);
        }

        // 该账户有逐仓margin账户,则异步更新其逐仓margin账户的TradeLevel
        if (userCommonBusiness.isExistIsolatedMarginAccount(user.getUserId())) {
            userInfoAsyncTask.setIsolatedMarginUserTradeLevel(userId, tradeLevel);
        }

        return APIResponse.getOKJsonResult(result);
    }

    public int setTradeLevel(final Long userId, final Integer tradeLevel) {
        int num = 0;
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo) {
            if (null == userInfo.getTradeLevel() || userInfo.getTradeLevel().compareTo(tradeLevel) != 0) {
                num = userInfoMapper.updateByTradeLevel(userInfo.getUserId(), tradeLevel);
                //升级才推送
                if (null == userInfo.getTradeLevel() || userInfo.getTradeLevel().compareTo(tradeLevel) < 0){
                    Map<String,Object> data = Maps.newHashMap();
                    data.put("viplevel",tradeLevel);
                    APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, data, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web","VIP_levelup");
                    AsyncTaskExecutor.execute(() -> {
                        try {
                            inboxMessageTextApi.pushInbox(apiRequest);
                        }catch (Exception e){
                            log.warn("send inbox ip update error",e);
                        }
                    });
                }


                // 将tradeLevel>=1的普通用户自动设置为母账户
                if (autoEnableSubUserFunctionDueToTradeLevel) {
                    try {
                        if (tradeLevel.compareTo(1) >= 0) {
                            User user = userCommonBusiness.checkAndGetUserById(userId);
                            // 通过实名认证，且为普通用户
                            if (BitUtils.isTrue(user.getStatus(), Constant.USER_CERTIFICATION)
                                    && userCommonBusiness.isNormalUser(user.getStatus())) {
                                APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
                                ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
                                parentUserIdReq.setParentUserId(userId);
                                subUserReq.setBody(parentUserIdReq);
                                APIResponse<Boolean> result = subUserAdminBusiness.enableSubUserFunction(subUserReq);
                                log.info("autoEnableSubUserFunction for tradeLevel>=1 parentUserId:{}, result:{}", userId, result);
                            }
                        }
                    } catch (Exception e) {
                        log.error("enableSubUserFunction error for tradeLevel>=1", e);
                    }
                }

            }
        } else {
            log.error("userInfo is null, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return num;
    }

    @Override
    public APIResponse<Integer> setTradeAutoStatus(APIRequest<SetTradeAutoStatus> request) {
        final SetTradeAutoStatus requestBody = request.getBody();
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(requestBody.getUserId());
        int num = 0;
        if (userInfo != null) {
            if (requestBody.getIsAutoUpdate() == true) {
                num = userInfoMapper.updateTradeAutoStatus(requestBody.getUserId(), "Y");
            } else {
                num = userInfoMapper.updateTradeAutoStatus(requestBody.getUserId(), "N");
            }
        }
        return APIResponse.getOKJsonResult(num);
    }

    @Override
    public APIResponse<Integer> setTradeLevelAndCommissionRequest(APIRequest<SetTradeLevelAndCommissionRequest> request)
            throws Exception {
        final SetTradeLevelAndCommissionRequest body = request.getBody();

        //broker子账户可以调用，普通子账户不允许
        Long userId=body.getUserId();
        User currentUser=userCommonBusiness.checkAndGetUserById(userId);
        Boolean isNormalSubUser = userCommonBusiness.isNormalSubUser(currentUser.getStatus());
        if(isNormalSubUser){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        APIRequest<SetTradeLevelRequest> levelRequest = new APIRequest();
        SetTradeLevelRequest statusBody = new SetTradeLevelRequest();
        statusBody.setUserId(body.getUserId());
        statusBody.setTradeLevel(body.getTradeLevel());
        levelRequest.setBody(statusBody);
        this.setTradeLevel(levelRequest);

        APIRequest<SetCommissionRequest> commissionRequest = new APIRequest();
        SetCommissionRequest commissionBody = new SetCommissionRequest();
        commissionBody.setUserId(body.getUserId());
        commissionBody.setBuyerCommission(body.getBuyerCommission());
        commissionBody.setSellerCommission(body.getSellerCommission());
        commissionBody.setTakerCommission(body.getTakerCommission());
        commissionBody.setMakerCommission(body.getMakerCommission());
        commissionBody.setModifyReason(body.getModifyReason());
        commissionBody.setExpectedRestoreTime(null);
        commissionRequest.setBody(commissionBody);
        this.setCommission(commissionRequest);

        return APIResponse.getOKJsonResult(1);
    }

    @Override
    public APIResponse<Integer> updatePnkTradingAccount() {
        try {
            // 查pnk的accountId为null的数据
            List<String> userIdList = oldUserMapper.getPnkTradingAccount();

            // 遍历account的数据，account的accountId为不为null的情况则是需要修复的数据，循环发mq到pnk 补偿数据
            userIdList.stream().forEach(strUserId -> {
                log.info("pnk需修复用户-->{}", strUserId);
                Long userId = Long.valueOf(strUserId);
                Long tradingAccount = userInfoMapper.selectAccountIdByUserId(userId);
                if (null != tradingAccount) {
                    // 发mq修复pnk的tradingAccount
                    updatePnkBySendMq(tradingAccount, userId);
                }
            });
            return APIResponse.getOKJsonResult(1);
        } catch (Exception e) {
            log.error("修复pnk的tradingAccount出错-->", e);
            return APIResponse.getErrorJsonResult(0);
        }
    }

    /**
     * 发mq到pnk修复pnk的tradingAccount数据
     *
     * @param accountId:tradingAccount
     * @param userId
     */
    private void updatePnkBySendMq(Long accountId, Long userId) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, userId);
        dataMsg.put("tradingAccount", accountId);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_ACTIVE, dataMsg);
        log.info("iMsgNotification trading_account:{}", JSON.toJSONString(msg));
        XxlJobLogger.log("iMsgNotification trading_account:{0}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        log.info("用户完成账号自检修复:{}", userId);
        XxlJobLogger.log("{0}用户完成账号自检修复", userId);
    }

    @Override
    public APIResponse<Integer> saveUserConfig(APIRequest<SetUserConfigRequest> request) {
        final SetUserConfigRequest body = request.getBody();
        UserConfig uc = new UserConfig();
        uc.setUserId(body.getUserId());
        uc.setConfigType(body.getConfigType());
        uc.setConfigName(body.getConfigName());
        try {

            String keyCode = String.valueOf(body.getUserId()) + "_" + body.getConfigType();
            // 没有默认配置项，则添加默认配置项 清除缓存
            iUserConfig.insertOrupdateUserConfig(keyCode, uc);
            return APIResponse.getOKJsonResult(1);
        } catch (Exception e) {
            log.error("设置用户默认配置项出错,configType:{}, error-->{}", body.getConfigType(), e);
            return APIResponse.getErrorJsonResult(0);
        }
    }

    @Override
    public APIResponse<List<UserConfigResponse>> selectUserConfig(APIRequest<SelectUserConfigRequest> request) {
        final SelectUserConfigRequest body = request.getBody();
        // 获取参数
        String configType = body.getConfigType();
        String exclude = body.getExclude();

        // configType和exclude不能同时有值
        if (!StringUtils.isEmpty(configType) && !StringUtils.isEmpty(exclude)) {
            return APIResponse.getErrorJsonResult("params error: 'configType' and 'exclude' either-or");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(UserConst.USER_ID, body.getUserId());
        List<String> typeList = null;
        if (!StringUtils.isEmpty(configType)) {
            typeList = new ArrayList<>(Arrays.asList(configType.split(",")));
            params.put("configTypes", typeList);
        }
        List<String> excludeList = null;
        if (!StringUtils.isEmpty(exclude)) {
            excludeList = new ArrayList<>(Arrays.asList(exclude.split(",")));
            params.put("excludes", excludeList);
        }
        List<UserConfig> ucList = null;
        if (!CollectionUtils.isEmpty(typeList) && typeList.size() == 1) {
            // 读缓存数据
            String keyCode = new StringBuilder().append(body.getUserId()).append("_").append(configType).toString();
            ucList = iUserConfig.selectUserConfigList(keyCode, params);
        } else {
            // 不读缓存
            ucList = userInfoMapper.selectUserConfigList(params);
        }
        List<UserConfigResponse> ucResponseList = new ArrayList<>();
        if (ucList != null) {
            ucList.forEach(uc -> {
                UserConfigResponse ucr = new UserConfigResponse();
                BeanUtils.copyProperties(uc, ucr);
                ucResponseList.add(ucr);

            });
        }
        return APIResponse.getOKJsonResult(ucResponseList);
    }

    @Override
    public APIResponse<GetUserAgentRewardResponse> getUserAgentList(APIRequest<AgentRewardRequest> request) {
        final AgentRewardRequest body = request.getBody();
        // 获取参数
        Long userId = body.getUserId();
        String email = body.getEmail();// 邮箱
        BigDecimal agentRewardRatio = body.getAgentRewardRatio();// 返佣比例新值
        /*
         * Date startExpectRestoreTime = body.getStartExpectRestoreTime();//预计恢复开始时间 Date
         * endExpectRestoreTime = body.getEndExpectRestoreTime();//预计恢复结束时间
         */
        int offset = body.getOffset();// 查询的起始条数
        int limit = body.getLimit();// 查询条数
        String reason = body.getReason();
        // 参数校验
        if (limit <= 0) {
            return APIResponse.getErrorJsonResult("param error");
        }
        if (offset < 0) {
            offset = 0;
        }

        GetUserAgentRewardResponse response = new GetUserAgentRewardResponse();
        // 参数设置
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, userId);
        paramMap.put(UserConst.EMAIL, body.getEmail());// 邮箱
        paramMap.put("agentRewardRatio", agentRewardRatio);
        paramMap.put("trackSource", body.getTrackSource());// 注册渠道
        /*
         * paramMap.put("startExpectRestoreTime", startExpectRestoreTime);
         * paramMap.put("endExpectRestoreTime", endExpectRestoreTime);
         */
        paramMap.put("agentId", body.getAgentId());// 推荐人
        paramMap.put("offset", offset);
        paramMap.put("limit", limit);
        String code = null;
        if (body.getIsRestore() != null) {
            code = body.getIsRestore().getCode();// 是否预计恢复时间
            paramMap.put(UserConst.IS_RESTORE, code);
        }
        paramMap.put("reason", reason);// 修改原因

        paramMap.put("excludeDefaultAgent", body.getExcludeDefaultAgent());
        paramMap.put("defaultAgent", body.getDefaultAgent());
        paramMap.put("startTime", body.getStartTime());
        paramMap.put("endTime", body.getEndTime());

        log.info("getUserAgentList paramMap-->{}", paramMap);

        // 查返佣集合列表
        // 1.是否预计恢复时间或修改原因不为null,直接查user_agent_reward
        if ((!StringUtils.isBlank(code) && "y".equalsIgnoreCase(code)) || !StringUtils.isBlank(reason)) {
            paramMap.put("status", 1);
            return getListFromAgentReward(paramMap, response);
        }
        // 2.如果邮箱不为null
        if (!StringUtils.isEmpty(email)) {
            return getListByParamEmail(paramMap, response);
        }
        // 3.是否预计恢复时间、修改原因和邮箱都为null，查user_info的分佣比例信息，联合user表，获得email
        return getListByParam(paramMap, response);
    }

    /**
     * 直接从user_agent_reward获取返佣比例集合
     *
     * @param paramMap
     * @param response
     * @return
     */
    @Monitored
    private APIResponse<GetUserAgentRewardResponse> getListFromAgentReward(Map<String, Object> paramMap,
            GetUserAgentRewardResponse response) {
        List<UserAgentReward> rewardList = this.userInfoMapper.getUserAgentRewardList(paramMap);
        if (rewardList == null || rewardList.size() <= 0) {
            response.setResult(null);
            response.setCount(this.userInfoMapper.getUserAgentRewardCount(paramMap));
            return APIResponse.getOKJsonResult(response);
        }

        List<UserAgentRewardVo> userAgentRewardVoList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rewardList.stream().forEach(reward -> {
            if (reward.getExpectRestoreTime() != null) {
                reward.setExpectRestoreTimeStr(sdf.format(reward.getExpectRestoreTime()));
            }

            if (reward.getActualRestoreTime() != null) {
                reward.setActualRestoreTimeStr(sdf.format(reward.getActualRestoreTime()));
            }

            if (reward.getUpdateTime() != null) {
                reward.setUpdateTimeStr(sdf.format(reward.getUpdateTime()));
            }

            UserInfo ui = this.userInfoMapper.selectByPrimaryKey(reward.getUserId());
            if (ui != null) {
                reward.setAgentRewardRatio(ui.getAgentRewardRatio());
                reward.setAgentId(ui.getAgentId());
            }

            UserAgentRewardVo rewardVo = new UserAgentRewardVo();
            BeanUtils.copyProperties(reward, rewardVo);
            userAgentRewardVoList.add(rewardVo);
        });
        response.setResult(userAgentRewardVoList);
        response.setCount(this.userInfoMapper.getUserAgentRewardCount(paramMap));
        return APIResponse.getOKJsonResult(response);
    }

    /**
     * 从user_info中获取返佣比例集合
     *
     * @param paramMap
     * @param response
     * @return
     */
    @Monitored
    private APIResponse<GetUserAgentRewardResponse> getListByParam(Map<String, Object> paramMap,
            GetUserAgentRewardResponse response) {
        String isRestore = String.valueOf(paramMap.get(UserConst.IS_RESTORE));
        List<UserInfo> userInfoList = this.userInfoMapper.getUserInfoAgentList(paramMap);
        Long count = this.userInfoMapper.getUserInfoRewardCount(paramMap);
        if (userInfoList == null || userInfoList.size() <= 0) {
            response.setResult(null);
            response.setCount(count);
            return APIResponse.getOKJsonResult(response);
        }
        List<Long> userIds = new ArrayList<>();
        userInfoList.stream().forEach(ui -> {
            userIds.add(ui.getUserId());
        });

        List<Map<String, Object>> emailUserIdList = this.userInfoMapper.getEmailByUserIds(userIds);
        if (emailUserIdList != null && emailUserIdList.size() > 0) {
            // 查UserAgentReward
            List<UserAgentReward> rewardList = null;
            if (userIds.size() > 0) {
                rewardList = userAgentRewardMapper.selectByUserIds(userIds);
            }

            List<UserAgentRewardVo> userAgentRewardVoList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < userInfoList.size(); i++) {
                UserAgentRewardVo rewardVo = new UserAgentRewardVo();
                UserInfo uinfo = userInfoList.get(i);
                for (int j = 0; j < emailUserIdList.size(); j++) {
                    Map<String, Object> map = emailUserIdList.get(j);
                    if (map.get(UserConst.USER_ID) != null
                            && Long.valueOf(String.valueOf(map.get(UserConst.USER_ID))).equals(uinfo.getUserId())) {
                        rewardVo.setEmail(String.valueOf(map.get(UserConst.EMAIL)));
                        break;
                    }
                }
                if (rewardList != null && rewardList.size() > 0) {
                    for (UserAgentReward reward : rewardList) {
                        if (StringUtils.equals(String.valueOf(uinfo.getUserId()), String.valueOf(reward.getUserId()))) {
                            if ("n".equalsIgnoreCase(isRestore) && "y".equalsIgnoreCase(reward.getIsRestore())) { // 为n的时候过滤为y的rewardVo
                                continue;
                            }
                            rewardVo.setReason(reward.getReason());
                            rewardVo.setIsRestore(reward.getIsRestore());
                            rewardVo.setId(reward.getId());
                            rewardVo.setIsRestore(reward.getIsRestore());
                            if (reward.getActualRestoreTime() != null) {
                                rewardVo.setActualRestoreTimeStr(sdf.format(reward.getActualRestoreTime()));
                            }
                            if (reward.getExpectRestoreTime() != null) {
                                rewardVo.setExpectRestoreTimeStr(sdf.format(reward.getExpectRestoreTime()));
                            }
                            rewardVo.setId(reward.getId());
                        }
                    }
                }
                rewardVo.setUserId(uinfo.getUserId());
                rewardVo.setAgentRewardRatio(uinfo.getAgentRewardRatio());
                if (uinfo.getUpdateTime() != null) {
                    rewardVo.setUpdateTimeStr(sdf.format(uinfo.getUpdateTime()));
                }
                if (uinfo.getInsertTime() != null) {
                    rewardVo.setInsertTimeStr(sdf.format(uinfo.getInsertTime()));
                }
                rewardVo.setAgentId(uinfo.getAgentId());
                rewardVo.setTrackSource(uinfo.getTrackSource());
                userAgentRewardVoList.add(rewardVo);
                response.setResult(userAgentRewardVoList);
            }
            response.setCount(this.userInfoMapper.getUserInfoRewardCount(paramMap));
        }
        return APIResponse.getOKJsonResult(response);
    }

    @Monitored
    private APIResponse<GetUserAgentRewardResponse> getListByParamEmail(Map<String, Object> paramMap,
            GetUserAgentRewardResponse response) {
        // 根据email查user表用户信息
        String email = String.valueOf(paramMap.get(UserConst.EMAIL));
        String isRestore = String.valueOf(paramMap.get("paramMap"));
        User user = this.userMapper.queryByEmail(email);
        if (user == null || user.getUserId() == null) {
            response.setResult(null);
            response.setCount(0L);
            return APIResponse.getOKJsonResult(response);
        }

        Long userId = user.getUserId();
        paramMap.put(UserConst.USER_ID, userId);
        List<UserInfo> userInfoList = this.userInfoMapper.getUserInfoAgentList(paramMap);
        Long count = this.userInfoMapper.getUserInfoRewardCount(paramMap);
        if (userInfoList == null || userInfoList.size() <= 0) {
            response.setResult(null);
            response.setCount(count);
            return APIResponse.getOKJsonResult(response);
        }
        // 查UserAgentReward
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        List<UserAgentReward> rewardList = null;
        if (userIds.size() > 0) {
            rewardList = userAgentRewardMapper.selectByUserIds(userIds);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<UserAgentRewardVo> userAgentRewardVoList = new ArrayList<>();

        for (UserInfo uinfo : userInfoList) {
            UserAgentRewardVo rewardVo = new UserAgentRewardVo();
            rewardVo.setEmail(email);
            rewardVo.setUserId(uinfo.getUserId());
            if (uinfo.getUpdateTime() != null) {
                rewardVo.setUpdateTimeStr(sdf.format(uinfo.getUpdateTime()));
            }
            rewardVo.setAgentId(uinfo.getAgentId());
            rewardVo.setTrackSource(uinfo.getTrackSource());
            rewardVo.setAgentRewardRatio(uinfo.getAgentRewardRatio());

            if (rewardList != null && rewardList.size() > 0) {
                for (UserAgentReward reward : rewardList) {
                    if (uinfo.getUserId() == reward.getUserId()) {
                        if ("n".equalsIgnoreCase(isRestore) && "y".equalsIgnoreCase(reward.getIsRestore())) { // 为n的时候过滤为y的rewardVo
                            continue;
                        }
                        rewardVo.setReason(reward.getReason());
                        rewardVo.setId(reward.getId());
                        rewardVo.setIsRestore(reward.getIsRestore());
                        if (reward.getActualRestoreTime() != null) {
                            rewardVo.setActualRestoreTimeStr(sdf.format(reward.getActualRestoreTime()));
                        }
                        if (reward.getExpectRestoreTime() != null) {
                            rewardVo.setExpectRestoreTimeStr(sdf.format(reward.getExpectRestoreTime()));
                        }

                    }
                }
            }

            userAgentRewardVoList.add(rewardVo);
            response.setResult(userAgentRewardVoList);
        }
        response.setCount(this.userInfoMapper.getUserInfoRewardCount(paramMap));
        return APIResponse.getOKJsonResult(response);
    }

    /**
     * 生成批次号
     *
     * @return
     */
    private String nextBatchId() {
        String currentBatchId = this.userAgentRewardMapper.getLastBatchId();
        Log.info("currentBatchId-->{}", currentBatchId);
        String nextBatchId = "1";
        if (!StringUtils.isBlank(currentBatchId)) {
            nextBatchId = (Integer.parseInt(currentBatchId) + 1) + "";
        }
        return nextBatchId;
    }

    @SecurityLog(name = "修改返佣比例--新版", operateType = Constant.SECURITY_OPERATE_TYPE_UPDATE_AGENT_REWARD_RATIO,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateUserAgentRatio(APIRequest<UpdateUserAgentRewardRequest> request) {

        UpdateUserAgentRewardRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();

        User user = userCommonBusiness.checkAndGetUserById(userId);

        // 更新用户返佣比例
        int result = updateUserAgentRatio(request.getBody());

        // 若为母账户，则更新子账户返佣比例
        if (userCommonBusiness.isParentUser(user.getStatus())) {
            userInfoAsyncTask.updateSubUsersAgentRatio(userId, requestBody);
        }

        return APIResponse.getOKJsonResult(result);
    }

    public int updateUserAgentRatio(UpdateUserAgentRewardRequest requestBody) {
        // 根据user_id查user_agent_reward表，查到比较该表的新值字段是否和当前值一样，一样不做操作，不一样则update，取新值作为旧值，
        // 没查到insert,insert旧值为user_info表修改之前的分佣比例，当前传入的分佣比例为新值
        Map<String, Object> params = Maps.newHashMap();
        Long userId = requestBody.getUserId();
        params.put("id", requestBody.getId());
        params.put("agentRewardRatio", requestBody.getAgentRewardRatio());
        params.put(UserConst.IS_RESTORE, requestBody.getIsRestore().getCode());
        params.put("expectRestoreTime", requestBody.getExpectRestoreTime());
        params.put("reason", requestBody.getReason());
        params.put("applyId", requestBody.getApplyId());
        params.put("applyName", requestBody.getApplyName());
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui == null) {// 该用户不存在
            return 0;
        }
        if (!StringUtils.isBlank(ui.getTrackSource())) {
            params.put("trackSource", ui.getTrackSource());// 注册来源
        }
        if (ui.getAgentRewardRatio() != null) {
            params.put("oldAgentRewardRatio", ui.getAgentRewardRatio());// 旧值
        }
        params.put("newAgentRewardRatio", requestBody.getAgentRewardRatio());// 新值
        log.info("updateUserAgentRatio params-->{}", new Gson().toJson(params));

        // 查UserAgentReward记录
        UserAgentReward uar = this.userInfoMapper.selectUserAgentRewardByUserId(requestBody.getUserId());
        if (uar != null && requestBody.getId() == null) {// 修改必须有id
            log.warn("userId为{}的用户有待审核的记录，但审核id为null,无法修改", userId);
            return 0;
        }
        if (uar != null) {// 查到，update user_agent_reward
            Date newDate = new Date();
            params.put("applyTime", newDate);// 申请时间
            params.put(UserConst.UPDATE_TIME, newDate);// 修改时间
            // update UserAgentReward记录并返回
            return this.userAgentRewardMapper.updateByPrimaryKeySelective(params);
        }
        // 没查到，insert user_agent_reward
        params.put(UserConst.BATCH_ID, nextBatchId());// 批次号
        params.put(UserConst.USER_ID, userId);

        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (!StringUtils.isBlank(userIndex.getEmail())) {
            params.put(UserConst.EMAIL, userIndex.getEmail());// 邮箱
        }
        Date newDate = new Date();
        params.put("applyTime", newDate);// 申请时间
        params.put(UserConst.CREATE_TIME, newDate);// 创建时间
        // insert 一条UserAgentReward记录并返回
        return this.userAgentRewardMapper.saveUserAgentReward(params);
    }

    /**
     * 批量修改分佣比例 修改user_info，修改或insert UserAgentReward
     */
    @Override
    public APIResponse<BatchUpdateAgentResponse> batchUpdateAgentId(
            APIRequest<UpdateUserAgentRewardListRequest> request) {

        final UpdateUserAgentRewardListRequest requestBody = request.getBody();
        List<UpdateUserAgentRewardRequest> userInfoList = requestBody.getAgentList();
        if (userInfoList == null || userInfoList.size() <= 0) {
            return APIResponse.getErrorJsonResult(0);
        }

        // 生成批次号
        String nextBatchId = nextBatchId();
        int total = batchUpdateAgentId(userInfoList, nextBatchId);
        BatchUpdateAgentResponse response = new BatchUpdateAgentResponse();
        response.setBatchId(nextBatchId);
        response.setTotal(total);
        return APIResponse.getOKJsonResult(response);
    }

    private int batchUpdateAgentId(final List<UpdateUserAgentRewardRequest> userInfoList, final String nextBatchId) {
        int total = 0;
        for (UpdateUserAgentRewardRequest userInfo : userInfoList) {
            total += batchUpdateAgentId(userInfo, nextBatchId);
        }
        return total;
    }

    public int batchUpdateAgentId(final UpdateUserAgentRewardRequest userInfo, final String nextBatchId) {
        int total = 0;
        Map<String, Object> params = Maps.newHashMap();
        Long userId = userInfo.getUserId();
        params.put(UserConst.USER_ID, userId);
        params.put("agentRewardRatio", userInfo.getAgentRewardRatio());
        params.put(UserConst.IS_RESTORE, userInfo.getIsRestore().getCode());
        params.put("expectRestoreTime", userInfo.getExpectRestoreTime());
        params.put("reason", userInfo.getReason());
        params.put("applyId", userInfo.getApplyId());
        params.put("applyName", userInfo.getApplyName());
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui != null) {// 该用户不存在
            if (!StringUtils.isBlank(ui.getTrackSource())) {
                params.put("trackSource", ui.getTrackSource());// 注册来源
            }
            if (ui.getAgentRewardRatio() != null) {
                params.put("oldAgentRewardRatio", ui.getAgentRewardRatio());// 旧值
            }
            params.put("newAgentRewardRatio", userInfo.getAgentRewardRatio());// 新值
            log.info("updateUserAgentRatio params-->{}", new Gson().toJson(params));

            // 查待审核的UserAgentReward记录
            UserAgentReward uar = this.userInfoMapper.selectUserAgentRewardByUserId(userInfo.getUserId());
            if (uar != null) {// 查到，update user_agent_reward
                Date newDate = new Date();
                params.put("applyTime", newDate);// 申请时间
                params.put(UserConst.UPDATE_TIME, newDate);// 修改时间
                params.put(UserConst.BATCH_ID, nextBatchId);// 批次号
                // update 待审核的UserAgentReward记录
                total += this.userAgentRewardMapper.updateAgentRewardByUserId(params);

            } else {// 没查到，insert user_agent_reward
                params.put(UserConst.BATCH_ID, nextBatchId);// 批次号
                final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
                if (!StringUtils.isBlank(userIndex.getEmail())) {
                    params.put(UserConst.EMAIL, userIndex.getEmail());// 邮箱
                }
                Date newDate = new Date();
                params.put("applyTime", newDate);// 申请时间
                params.put(UserConst.CREATE_TIME, newDate);// 创建时间
                // insert 一条UserAgentReward记录
                total += this.userAgentRewardMapper.saveUserAgentReward(params);
            }
        }

        // 若为母账户，则更新子账户
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            if (userCommonBusiness.isParentUser(user.getStatus())) {
                userInfoAsyncTask.batchUpdateSubUsersAgentId(userId, userInfo, nextBatchId);
            }
        } catch (Exception e) {
            log.warn("batchUpdateSubUsersAgentId error", e);
        }

        return total;
    }

    @Override
    public APIResponse<GetUserAgentRewardResponse> getUserAgentRewardList(APIRequest<AgentRewardAuditRequest> request) {
        final AgentRewardAuditRequest body = request.getBody();
        // 获取参数
        Long userId = body.getUserId();
        int offset = body.getOffset();// 查询的起始条数
        int limit = body.getLimit();// 查询条数
        // 参数校验
        if (limit <= 0) {
            return APIResponse.getErrorJsonResult("param error");
        }
        if (offset < 0) {
            offset = 0;
        }

        GetUserAgentRewardResponse response = new GetUserAgentRewardResponse();
        // 参数设置
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, userId);
        paramMap.put(UserConst.EMAIL, body.getEmail());// 邮箱
        paramMap.put(UserConst.BATCH_ID, body.getBatchId());// 批次号
        if (body.getStatus() != null) {
            paramMap.put("status", body.getStatus().getCode());// 审核状态
        }
        if (body.getIsRestore() != null) {
            paramMap.put(UserConst.IS_RESTORE, body.getIsRestore().getCode());// 是否预计恢复时间
        }
        paramMap.put("batch", body.getBatch());// 是否按照批次号审核
        paramMap.put("startApplyTime", body.getStartApplyTime());// 申请开始时间
        paramMap.put("endApplyTime", body.getEndApplyTime());// 申请结束时间
        paramMap.put("startOperatorTime", body.getStartOperatorTime());// 审核开始时间
        paramMap.put("endOperatorTime", body.getEndOperatorTime());// 审核结束时间
        paramMap.put("reason", body.getReason());// 修改原因
        paramMap.put("offset", offset);
        paramMap.put("limit", limit);
        if (body.getOrder() != null) {
            paramMap.put("sort", body.getSort());
            paramMap.put("order", body.getOrder().getCode());
        }

        log.info("getUserAgentList paramMap-->{}", paramMap);
        return getAgentRewardList(paramMap, response);
    }

    /**
     * 条件查询user_agent_reward表
     *
     * @param paramMap
     * @param response
     * @return
     */
    @Monitored
    private APIResponse<GetUserAgentRewardResponse> getAgentRewardList(Map<String, Object> paramMap,
            GetUserAgentRewardResponse response) {
        List<UserAgentReward> rewardList = this.userAgentRewardMapper.getAgentRewardList(paramMap);
        if (rewardList == null || rewardList.size() <= 0) {
            response.setResult(null);
            response.setCount(this.userAgentRewardMapper.getUserAgentRewardNum(paramMap));
            return APIResponse.getOKJsonResult(response);
        }

        List<UserAgentRewardVo> userAgentRewardVoList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rewardList.stream().forEach(reward -> {
            /*
             * log.info("ExpectRestoreTime:{},ActualRestoreTime:{},ApplyTime:{},OperatorTime:{}",
             * reward.getExpectRestoreTime(),reward.getActualRestoreTime(),reward.getApplyTime(),reward.
             * getOperatorTime());
             */
            if (reward.getExpectRestoreTime() != null) {
                reward.setExpectRestoreTimeStr(sdf.format(reward.getExpectRestoreTime()));
            }

            if (reward.getActualRestoreTime() != null) {
                reward.setActualRestoreTimeStr(sdf.format(reward.getActualRestoreTime()));
            }

            if (reward.getApplyTime() != null) {
                reward.setApplyTimeStr(sdf.format(reward.getApplyTime()));
            }

            if (reward.getOperatorTime() != null) {
                reward.setOperatorTimeStr(sdf.format(reward.getOperatorTime()));
            }

            if (reward.getUpdateTime() != null) {
                reward.setUpdateTimeStr(sdf.format(reward.getUpdateTime()));
            }

            UserAgentRewardVo rewardVo = new UserAgentRewardVo();
            BeanUtils.copyProperties(reward, rewardVo);
            userAgentRewardVoList.add(rewardVo);
        });
        response.setResult(userAgentRewardVoList);
        response.setCount(this.userAgentRewardMapper.getUserAgentRewardNum(paramMap));
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<ResultDateResponse> updateAgentStatus(APIRequest<AgentStatusRequest> request) {
        AgentStatusRequest body = request.getBody();
        log.info("ids:{},status:{},operatorId:{}", new Gson().toJson(body.getIds()), body.getStatus().getCode(),
                body.getOperatorId());
        ResultDateResponse response = new ResultDateResponse();
        Date nowDate = new Date();

        List<Long> allIds = new ArrayList<>();
        allIds.addAll(body.getIds());

        // 若有母账户，则更新其子账户
        for (Long id : body.getIds()) {
            try {
                User user = userCommonBusiness.checkAndGetUserById(id);
                if (userCommonBusiness.isParentUser(user.getStatus())) {
                    List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(user.getUserId());
                    if (CollectionUtils.isNotEmpty(subUserIds)) {
                        allIds.addAll(subUserIds);
                    }
                }
            } catch (Exception e) {
                log.error("updateAgentStatus set allIds error", e);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("status", body.getStatus().getCode());
        map.put("operatorId", body.getOperatorId());// 审核人id
        map.put("operatorTime", nowDate);
        map.put(UserConst.UPDATE_TIME, nowDate);
        // 根据ids查userAgentReward
        List<UserAgentReward> agentList = userAgentRewardMapper.selectByIds(allIds);

        if (agentList != null && agentList.size() > 0 && !StringUtils.isBlank(body.getOperatorId())) {
            for (UserAgentReward uar : agentList) {
                if (body.getOperatorId().equals(uar.getApplyId())) {
                    response.setData("-1");
                    response.setDesc(String.format("审核人不能为申请人,用户:%s的审核人为%s,申请人为%s", String.valueOf(uar.getId()),
                            body.getOperatorId(), uar.getApplyId()));
                    return APIResponse.getErrorJsonResult(response);
                }
            }
        }

        Integer total = 0;
        // update userAgentReward
        for (Long id : allIds) {
            map.put("id", id);
            total += userAgentRewardMapper.updateByPrimaryKeySelective(map);
        }

        if (1 != body.getStatus().getCode()) {// 审核未通过，无需update user_info
            response.setData(String.valueOf(total));
            response.setDesc("审核成功");
            return APIResponse.getOKJsonResult(response);
        }

        // 审核通过的update user_info的返佣比例
        if (agentList != null && agentList.size() > 0) {
            for (UserAgentReward uar : agentList) {
                updateUserInfo(uar.getOldAgentRewardRatio(), uar.getNewAgentRewardRatio(), uar.getUserId());
            }
        }
        response.setData(String.valueOf(total));
        response.setDesc("审核成功");
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<ResultDateResponse> updateAgentStatusByBatchId(APIRequest<AgentStatusByBatchIdRequest> request) {
        ResultDateResponse response = new ResultDateResponse();
        AgentStatusByBatchIdRequest body = request.getBody();
        List<String> batchIds = body.getBatchIds();
        Date nowDate = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("status", body.getStatus().getCode());
        map.put("operatorId", body.getOperatorId());
        map.put("operatorTime", nowDate);
        map.put(UserConst.UPDATE_TIME, nowDate);

        // 根据批次号查userAgentReward
        List<UserAgentReward> agentList = userAgentRewardMapper.selectByBatchIds(batchIds);

        if (agentList != null && agentList.size() > 0 && !StringUtils.isBlank(body.getOperatorId())) {
            for (UserAgentReward uar : agentList) {
                if (body.getOperatorId().equals(uar.getApplyId())) {
                    response.setData("-1");
                    response.setDesc(String.format("审核人不能为申请人,用户:%s的审核人为%s,申请人为%s", String.valueOf(uar.getId()),
                            body.getOperatorId(), uar.getApplyId()));
                    return APIResponse.getErrorJsonResult(response);
                }
            }
        }

        Integer total = 0;
        for (String batchId : batchIds) {
            map.put(UserConst.BATCH_ID, batchId);
            total += userAgentRewardMapper.updateAgentStatusByBatchId(map);
        }
        if (1 != body.getStatus().getCode()) {
            response.setData(String.valueOf(total));
            response.setDesc("审核成功");
            return APIResponse.getOKJsonResult(response);
        }
        // 审核通过的update user_info的返佣比例
        if (agentList != null && agentList.size() > 0) {
            for (UserAgentReward uar : agentList) {
                updateUserInfo(uar.getOldAgentRewardRatio(), uar.getNewAgentRewardRatio(), uar.getUserId());
                //更新子账户
                User user = userCommonBusiness.checkAndGetUserById(uar.getUserId());
                if (userCommonBusiness.isParentUser(user.getStatus())) {
                    userInfoAsyncTask.updateSubUserAgentRewardRatio(uar.getUserId(), uar.getNewAgentRewardRatio());
                }
            }
        }
        response.setData(String.valueOf(total));
        response.setDesc("审核成功");
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<AuditAgentStatusByBatchIdResponse> auditAgentStatusByBatchId(APIRequest<AuditAgentStatusByBatchIdRequest> request) throws Exception {
        AuditAgentStatusByBatchIdResponse response = new AuditAgentStatusByBatchIdResponse();
        AuditAgentStatusByBatchIdRequest body = request.getBody();
        List<String> batchIds = body.getBatchIds();
        Date nowDate = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("status", body.getStatus().getCode());
        map.put("operatorId", body.getOperatorId());
        map.put("operatorTime", nowDate);
        map.put(UserConst.UPDATE_TIME, nowDate);

        // 根据批次号查userAgentReward
        List<UserAgentReward> agentList = userAgentRewardMapper.selectByBatchIds(batchIds);

        if (agentList != null && agentList.size() > 0 && !StringUtils.isBlank(body.getOperatorId())) {
            for (UserAgentReward uar : agentList) {
                if (body.getOperatorId().equals(uar.getApplyId())) {
                    response.setData("-1");
                    response.setDesc(String.format("审核人不能为申请人,用户:%s的审核人为%s,申请人为%s", String.valueOf(uar.getId()),
                            body.getOperatorId(), uar.getApplyId()));
                    return APIResponse.getErrorJsonResult(response);
                }
            }
        }

        Integer total = 0;
        for (String batchId : batchIds) {
            map.put(UserConst.BATCH_ID, batchId);
            total += userAgentRewardMapper.updateAgentStatusByBatchId(map);
        }
        if (1 != body.getStatus().getCode()) {
            response.setData(String.valueOf(total));
            response.setDesc("审核成功");
            return APIResponse.getOKJsonResult(response);
        }
        // 审核通过的update user_info的返佣比例
        Set<Long> affectUserIds = new HashSet<>();
        if (agentList != null && agentList.size() > 0) {
            for (UserAgentReward uar : agentList) {
                updateUserInfo(uar.getOldAgentRewardRatio(), uar.getNewAgentRewardRatio(), uar.getUserId());
                affectUserIds.add(uar.getUserId());
                //更新子账户
                User user = userCommonBusiness.checkAndGetUserById(uar.getUserId());
                if (userCommonBusiness.isParentUser(user.getStatus())) {
                    List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(uar.getUserId());
                    if (CollectionUtils.isNotEmpty(subUserIds)) {
                        affectUserIds.addAll(subUserIds);
                        userInfoAsyncTask.updateSubUserAgentRewardRatio(uar.getUserId(), uar.getNewAgentRewardRatio());
                    }
                }
            }
        }
        response.setData(String.valueOf(total));
        response.setDesc("审核成功");
        response.setAffectUserIds(Lists.newArrayList(affectUserIds));
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<List<Long>> selectAgentUserIdByBatchIds(APIRequest<AgentStatusByBatchIdRequest> request) {
        AgentStatusByBatchIdRequest body = request.getBody();
        List<String> batchIds = body.getBatchIds();
        // 根据批次号查userAgentReward
        List<UserAgentReward> agentList = userAgentRewardMapper.selectByBatchIds(batchIds);
        if (CollectionUtils.isEmpty(agentList) || agentList.size() <= 0){
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<Long> userIds = Lists.newArrayList();
        for (UserAgentReward userAgentReward:agentList){
            userIds.add(userAgentReward.getAgentId());
            User user = userCommonBusiness.checkAndGetUserById(userAgentReward.getUserId());
            if (userCommonBusiness.isParentUser(user.getStatus())) {
                List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(userAgentReward.getUserId());
                if (CollectionUtils.isNotEmpty(subUserIds)){
                    userIds.addAll(subUserIds);
                }
            }
        }
        return APIResponse.getOKJsonResult(userIds);
    }

    @Override
    public UserParentOrRootRelationShipByUserIdResp userParentOrRootRelationShipByUserId(UserIdRequest request) throws Exception {
        final String userId = request.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(Long.valueOf(userId));
        if (null == userInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserTypeEnum userTypeEnum=getUserType(Long.valueOf(userId));
        UserParentOrRootRelationShipByUserIdResp resp=new UserParentOrRootRelationShipByUserIdResp();
        resp.setUserTypeEnum(userTypeEnum);
        if(UserTypeEnum.SUB==userTypeEnum || UserTypeEnum.BROKER_SUB==userTypeEnum){
            resp.setParentUserId(userInfo.getParent());
        }else if(UserTypeEnum.MARGIN==userTypeEnum){
            UserInfo userQuery = new UserInfo();
            userQuery.setMarginUserId(Long.valueOf(userId));
            List<UserInfo> rootUserIds = userInfoMapper.queryUserInfoByUserId(userQuery);
            if(org.apache.commons.collections.CollectionUtils.isEmpty(rootUserIds) || rootUserIds.size() > 1){
                log.warn("userParentOrRootRelationShipByUserId:query by Id failed userId={}", userId);
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            resp.setRootUserId(rootUserIds.get(0).getUserId());
            resp.setParentUserId(rootUserIds.get(0).getParent());
        }else if(UserTypeEnum.PARENT==userTypeEnum || UserTypeEnum.BROKER_PARENT==userTypeEnum){
            //是母账号的话就什么都不要设置了
        }else if(UserTypeEnum.FUTURE==userTypeEnum){
            UserInfo userQuery = new UserInfo();
            userQuery.setFutureUserId(Long.valueOf(userId));
            List<UserInfo> rootUserIds = userInfoMapper.queryUserInfoByUserId(userQuery);
            if(org.apache.commons.collections.CollectionUtils.isEmpty(rootUserIds) || rootUserIds.size() > 1){
                log.warn("userParentOrRootRelationShipByUserId:query by Id failed userId={}", userId);
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            resp.setRootUserId(rootUserIds.get(0).getUserId());
            resp.setParentUserId(rootUserIds.get(0).getParent());
        }else if(UserTypeEnum.FIAT==userTypeEnum){
            UserInfo userQuery = new UserInfo();
            userQuery.setFiatUserId(Long.valueOf(userId));
            List<UserInfo> rootUserIds = userInfoMapper.queryUserInfoByUserId(userQuery);
            if(org.apache.commons.collections.CollectionUtils.isEmpty(rootUserIds) || rootUserIds.size() > 1){
                log.warn("userParentOrRootRelationShipByUserId:query by Id failed userId={}", userId);
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            resp.setRootUserId(rootUserIds.get(0).getUserId());
            resp.setParentUserId(rootUserIds.get(0).getParent());
        }else if(UserTypeEnum.ISOLATED_MARGIN==userTypeEnum){
            IsolatedMarginUserBinding isolatedMarginUserBinding=isolatedMarginUserBindingMapper.selectByIsolatedMarginUserId(Long.valueOf(userId));
            if(null==isolatedMarginUserBinding){
                log.warn("userParentOrRootRelationShipByUserId:query by Id failed userId={}", userId);
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            UserInfo rootUserInfo = this.userInfoMapper.selectByPrimaryKey(isolatedMarginUserBinding.getRootUserId());
            if (null == rootUserInfo) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            resp.setRootUserId(rootUserInfo.getUserId());
            resp.setParentUserId(rootUserInfo.getParent());
        }
        return resp;
    }

    @Override
    public APIResponse<UserInfoRewardRatioResponse> selectUserInfoRewardRatio(APIRequest<BaseDetailRequest> request) {
        UserInfoRewardRatioResponse response = new UserInfoRewardRatioResponse();
        BaseDetailRequest body = request.getBody();
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(body.getUserId());
        if (ui != null) {
            response.setAgentRewardRatio(ui.getAgentRewardRatio());
            return APIResponse.getOKJsonResult(response);
        }
        return APIResponse.getErrorJsonResult(0);
    }

    @Override
    public APIResponse<UserInfoVo> getUserInfoByUserId(APIRequest<UserIdRequest> request) throws Exception {
        UserIdRequest requestBody = request.getBody();
        final String userId = requestBody.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(Long.valueOf(userId));
        if (null == userInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return APIResponse.getOKJsonResult(userInfoVo);
    }

    public UserTypeEnum getUserType(Long userId){
        //先不要判断broker账号，因为broker权限和字母账号一样，再做区分没有意义，而且
        //会引起调用方错误
        UserTypeEnum userTypeEnum=UserTypeEnum.NORMAL;
        User user = userCommonBusiness.checkAndGetUserById(userId);
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        if(userStatusEx.getIsBrokerSubUserFunctionEnabled()){
            userTypeEnum=UserTypeEnum.BROKER_PARENT;
        }else if(userStatusEx.getIsBrokerSubUser()){
            userTypeEnum=UserTypeEnum.BROKER_SUB;
        }else if(userStatusEx.getIsSubUser()){
            userTypeEnum=UserTypeEnum.SUB;
        }else if(userStatusEx.getIsMarginUser()){
            userTypeEnum=UserTypeEnum.MARGIN;
        }else if(userStatusEx.getIsSubUserFunctionEnabled()){
            userTypeEnum=UserTypeEnum.PARENT;
        }else if(userStatusEx.getIsFutureUser()){
            userTypeEnum=UserTypeEnum.FUTURE;
        }else if(userStatusEx.getIsFiatUser()){
            userTypeEnum=UserTypeEnum.FIAT;
        }else if(userStatusEx.getIsIsolatedMarginUser()){
            userTypeEnum=UserTypeEnum.ISOLATED_MARGIN;
        }
        return userTypeEnum;
    }

    @Override
    public UserInfoVo getUserInfoByUserId(Long userId) throws Exception {
        UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        if (null == userInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return userInfoVo;
    }

    @Override
    public APIResponse<List<UserInfoVo>> getUserInfosByUserIds(APIRequest<List<Long>> request) throws Exception {
        List<Long> userIds = request.getBody();
        if (CollectionUtils.isEmpty(userIds) || userIds.size() == 0) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        if (userIds.size() > 500){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        List<UserInfo> userInfos = this.userInfoMapper.selectUserInfoList(userIds);
        if (CollectionUtils.isEmpty(userInfos)){
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<UserInfoVo> userInfoVos = new ArrayList<>(userInfos.size());
        for (UserInfo userInfo:userInfos){
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(userInfo, userInfoVo);
            userInfoVos.add(userInfoVo);
        }
        return APIResponse.getOKJsonResult(userInfoVos);
    }

    @Override
    public APIResponse<UserCommissionResponse> getUserCommission(APIRequest<IdLongRequest> request) {
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(request.getBody().getUserId());
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        UserCommissionResponse response = new UserCommissionResponse();
        response.setUserId(userInfo.getUserId());
        response.setBuyerCommission(userInfo.getBuyerCommission());
        response.setSellerCommission(userInfo.getSellerCommission());
        response.setMakerCommission(userInfo.getMakerCommission());
        response.setTakerCommission(userInfo.getTakerCommission());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<List<BigDecimal>> getUserLevelWithdreaw(APIRequest<UserIdRequest> request) {
        List<OldSecurityPolicy> securityPolicys = oldSecurityPolicyMapper.selectAll();
        Map<String, BigDecimal> securityPolicyMap = new HashMap<>();
        for (OldSecurityPolicy oldSecurityPolicy : securityPolicys) {
            securityPolicyMap.put(String.valueOf(oldSecurityPolicy.getSecurityLevel()),
                    oldSecurityPolicy.getWithdraw());
        }
        BigDecimal totalWithdrawAsset =
                oldUserDataMapper.getTotalWithdrawAssetNum(String.valueOf(request.getBody().getUserId()));
        List<BigDecimal> data = new ArrayList<>();
        data.add(securityPolicyMap.get("1"));
        data.add(securityPolicyMap.get("2"));
        if (totalWithdrawAsset == null || totalWithdrawAsset.compareTo(BigDecimal.ZERO) <= 0) {
            data.add(securityPolicyMap.get("3"));
        } else {
            data.add(totalWithdrawAsset);
        }
        return APIResponse.getOKJsonResult(data);
    }

    @Override
    public Integer updateUserDailyWithdrawCap(UpdateDailyWithdrawCapRequest request) {
        final Long userId = request.getUserId();
        final BigDecimal dailyWithdrawCap = request.getDailyWithdrawCap();

        userCommonBusiness.checkAndGetUserById(userId);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setDailyWithdrawCap(dailyWithdrawCap);
        int result = userInfoMapper.updateByPrimaryKeySelective(userInfo);
        log.info("updateUserDailyWithdrawCap done, userId:{}, dailyWithdrawCap:{}, result:{}", userId, dailyWithdrawCap,
                result);

        return result;
    }

    @Override
    public BigDecimal getUserDailyWithdrawCap(UserIdRequest request) {
        if (StringUtils.isBlank(request.getUserId())) {
            log.warn("getUserDailyWithdrawCap userId is null");
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        final Long userId = Long.valueOf(request.getUserId());
        userCommonBusiness.checkAndGetUserById(userId);

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null == userInfo) {
            log.warn("userInfo is null, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        BigDecimal dailyWithdrawCap = userInfo.getDailyWithdrawCap();

        log.info("getUserDailyWithdrawCap userId:{}, dailyWithdrawcap:{}", userId, dailyWithdrawCap);

        return dailyWithdrawCap;
    }

    @Override
    public Integer updateUserDailyFiatWithdrawCap(UpdateDailyFiatWithdrawCapRequest request) throws Exception {
        final Long userId = request.getUserId();
        final BigDecimal dailyFiatWithdrawCap = request.getDailyFiatWithdrawCap();
        final BigDecimal singleFiatWithdrawCap = request.getSingleFiatWithdrawCap();

        userCommonBusiness.checkAndGetUserById(userId);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setDailyFiatWithdrawCap(dailyFiatWithdrawCap);
        userInfo.setSingleFiatWithdrawCap(singleFiatWithdrawCap);
        int result = userInfoMapper.updateByPrimaryKeySelective(userInfo);
        log.info("updateUserFiatWithdrawCap done, userId:{}, dailyFiatWithdrawCap:{}, singleFiatWithdrawCap:{}, result:{}", userId, dailyFiatWithdrawCap,
                singleFiatWithdrawCap,result);

        return result;
    }

    @Override
    public String getUserLastLoginLanguage(Long userId) throws Exception {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType("userLastLoginLanguage");
        UserConfig result=userInfoMapper.selectLatestUserConfig(uc);
        if(null==result){
            return null;
        }
        return result.getConfigName();
    }

    @Override
    public List<UserGroupVo> allUserGroup() throws Exception {
        //存在parent的用户
        List<RelationUserInfo> parents = this.userInfoMapper.selectUSParentGroup();
        log.info("us allUserGroup parent size:{}", parents == null ? 0 : parents.size());
        //存在margin_user_id的用户
        List<RelationUserInfo> margins = this.userInfoMapper.selectUSMarginGroup();
        log.info("us allUserGroup margin size:{}", margins == null ? 0 : margins.size());
        Map<Long, List<Long>> maps = Streams.concat(parents.stream(), margins.stream())
                .collect(Collectors.groupingBy(e->e.getParentUserId(), Collectors.mapping(RelationUserInfo::getUserId, Collectors.toList())));
        List<UserGroupVo> vos = new ArrayList<>(maps.size());
        maps.forEach((k, v)->{
            UserGroupVo vo = new UserGroupVo();
            vo.setParentUserId(k);
            vo.setUserIds(new HashSet<>(v));
            vos.add(vo);
        });
        return vos;
    }

    @Override
    public List<Long> batchUpdateUserTradeLevelAndCommission(List<SetTradeLevelAndCommissionRequest> request) {
        List<Long> userIds = request.stream().map(e->e.getUserId()).collect(Collectors.toList());
        List<UserInfo> userInfos = this.userInfoMapper.selectUserInfoList(userIds);
        List<Long> successUserIds = new ArrayList<>();
        for(SetTradeLevelAndCommissionRequest remote : request){
            for(UserInfo info : userInfos){
                boolean isCommissionChange = info.getMakerCommission().compareTo(remote.getMakerCommission()) != 0
                        || info.getTakerCommission().compareTo(remote.getTakerCommission()) != 0
                        || info.getSellerCommission().compareTo(remote.getSellerCommission()) != 0
                        || info.getBuyerCommission().compareTo(remote.getBuyerCommission()) != 0;
                boolean isLevelChange = info.getTradeLevel().compareTo(remote.getTradeLevel()) != 0;
                log.info("us batchUpdateUserTradeLevelAndCommission userId:{}", info.getUserId());
                if(isCommissionChange || isLevelChange){
                    try {
                        this.setTradeLevelAndCommissionRequest(APIRequest.instance(remote));
                        successUserIds.add(info.getUserId());
                    } catch (Exception e) {
                        log.warn("us batchUpdateUserTradeLevelAndCommission exception:", e);
                    }
                }
            }
        }
        return successUserIds;
    }

    @Override
    public AccountUnifyUserInfoResponse selectUnifyUserInfo(AccountUnifyUserInfoRequest body) throws Exception {
        if (StringUtils.isBlank(body.getEmail()) && body.getUserId() == null){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (StringUtils.isNotBlank(body.getEmail()) &&  (!Pattern.matches(UserBusiness.REGEX_EMAIL, body.getEmail()) || body.getEmail().length() > UserBusiness.MAX_EMAIL_LENGTH)){
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        AccountUnifyUserInfoResponse response = new AccountUnifyUserInfoResponse();
        User user = null;
        if (body.getUserId() != null){
            user =userCommonBusiness.getUserByUseryId(body.getUserId());
        }else{
            user = userMapper.queryByEmail(body.getEmail());
        }
        if (user == null){
            log.warn("selectUnifyUserInfo.user is null,param:{}", JsonUtils.toJsonHasNullKey(body));
            return null;
        }
        response.setEmail(user.getEmail());
        response.setUserId(user.getUserId());
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (userSecurity != null){
            if (userSecurity.getMobileCode() != null){
                response.setCellphone(userSecurity.getMobileCode()+userSecurity.getMobile());
            }
            response.setWithdrawSecurityAutoStatus(userSecurity.getWithdrawSecurityAutoStatus());
            response.setWithdrawSecurityStatus(userSecurity.getWithdrawSecurityStatus());
            response.setWithdrawFaceStatus(userSecurity.getWithdrawSecurityFaceStatus());
            if (userSecurity.getSecurityLevel() != null && userSecurity.getSecurityLevel() == 2){
                response.setIdentityStatus(1);
            }else{
                response.setIdentityStatus(0);
            }
        }
        UserSecurityLog lastLoginLogByUserId = userSecurityLogMapper.getLastLoginLogByUserId(user.getUserId());
        response.setLastLoginTime(lastLoginLogByUserId==null?null:lastLoginLogByUserId.getOperateTime());
        response.setLoginForbid(BitUtils.isEnable(user.getStatus(), Constant.USER_LOGIN)?0:1);
        response.setTradeForbid(BitUtils.isEnable(user.getStatus(), Constant.USER_TRADE)?0:1);
        KycCertificateResult kycStatusByUserId = userCommonBusiness.getKycStatusByUserId(user.getUserId());
        if (kycStatusByUserId != null && kycStatusByUserId.getCertificateType() != null &&  kycStatusByUserId.getCertificateType()== 1){
            response.setName(kycStatusByUserId.getFirstName()+kycStatusByUserId.getMiddleName()+kycStatusByUserId.getLastName());
        }else if (kycStatusByUserId != null && kycStatusByUserId.getCertificateType() != null && kycStatusByUserId.getCertificateType() == 2){
            response.setName(kycStatusByUserId.getCompanyName());
        }
        long subUserNums = subUserBindingMapper.countSubUsersByParentUserId(user.getUserId());
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        int num = Integer.parseInt(String.valueOf(subUserNums))+(userInfo.getMarginUserId()!=null?1:0)+(userInfo.getFutureUserId()!=null?1:0)+(userInfo.getFiatUserId()!=null?1:0);
        response.setRelevantAccountNum(num);
        response.setRegisterTime(user.getInsertTime());
        response.setTradeLevel(userInfo.getTradeLevel());
        response.setAccountType(AccountTypeEnum.getAccountType(user.getStatus()));
        AccountTypeEnum accountTypeEnum = AccountTypeEnum.getAccountTypeEnumByAccountType(response.getAccountType());
        response.setAccountTypeDesc(accountTypeEnum== null?null:accountTypeEnum.getDesc());
        GetWithdrawMessageResponse withdrawMessage = capitalClient.getWithdrawMessage(user.getUserId());
        if (withdrawMessage != null){
            response.setTotalWithdrawAmount(withdrawMessage.getTotalWithdrawAmount());
            response.setRemainWithdrawAmount(withdrawMessage.getPrevWithdrawAmount()==null?withdrawMessage.getTotalWithdrawAmount():withdrawMessage.getTotalWithdrawAmount().subtract(withdrawMessage.getPrevWithdrawAmount()));
        }
        response.setDisable(BitUtils.isEnable(user.getStatus(), Constant.USER_DISABLED));

        return response;
    }


    @Override
    public Boolean selectIfHasAgentUser(SelectIfHasAgenUserRequest body) throws Exception {
        userCommonBusiness.checkAndGetUserById(body.getUserId());
        Map<String,Object> searchParam = Maps.newHashMap();
        searchParam.put("userId",body.getUserId());
        searchParam.put("startTime",body.getStartTime());
        searchParam.put("endTime",body.getEndTime());
        long result = userAgentLogMapper.selectAgentNumByRegisterTime(searchParam);
        return result>0;
    }

    @Override
    public Long selectParentBrokerUserId(Long futureUserId) throws Exception {
        UserInfo userInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
        if (userInfo == null){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(userInfo.getUserId());
        if (subUserBinding == null){
            return null;
        }
        User user = userCommonBusiness.checkAndGetUserById(subUserBinding.getParentUserId());
        if (!BitUtils.isEnable(user.getStatus(), Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {
            return null;
        }
        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if(parentUserInfo != null && parentUserInfo.getFutureUserId() != null){
            return parentUserInfo.getFutureUserId();
        }
        return null;
    }

    @Override
    public SelectUserRegisterTimeResponse selectRegisterTimeByUserId(Long userId)throws Exception{
        if (userId == null){
            return null;
        }
        User user = userCommonBusiness.checkAndGetUserById(userId);
        SelectUserRegisterTimeResponse res = new SelectUserRegisterTimeResponse();
        res.setRegisterTime(user.getInsertTime());
        res.setUserId(userId);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo != null && userInfo.getFutureUserId() != null){
            UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(userInfo.getFutureUserId());
            res.setFutureRegisterTime(futureUserInfo.getInsertTime());
        }
        if (userInfo != null && userInfo.getMarginUserId() != null){
            UserInfo marginUserInfo = userInfoMapper.selectByPrimaryKey(userInfo.getMarginUserId());
            res.setMarginRegisterTime(marginUserInfo.getInsertTime());
        }
        return res;

    }

    @Override
    public Map<String,Object> getAccountStatusForWapi(Long userId){
        log.info("UserInfoBusiness.getAccountStatusForWapi.userId:{}",userId);
        User user = userCommonBusiness.getUserByUseryId(userId);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (user != null) {
            //禁用交易
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_TRADE)) {
                RiskSecurityVo riskSecurityVo = riskSecurityApiClient.getBanCaseByUserId(userId);
                if (riskSecurityVo != null) {
                    int type = riskSecurityVo.getType();
                    if (type == 1) {
                        String time = sysConfigVarCacheService.getValue("unban_turnover_time");
                        resultMap.put(SUCCESS, true);
                        resultMap.put(MSG, "order.low_fill_rate");
                        resultMap.put("objs", new Object[]{time});
                    } else if (type == 2) {
                        String time = sysConfigVarCacheService.getValue("abnormal_order_time");
                        resultMap.put(SUCCESS, true);
                        resultMap.put(MSG, "order.abnormal_orders");
                        resultMap.put("objs", new Object[]{time});
                    } else if (type == 3) {
                        String time = sysConfigVarCacheService.getValue("unban_abnormal_trade_time");
                        resultMap.put(SUCCESS, true);
                        resultMap.put(MSG, "order.abnormal_trading");
                        resultMap.put("objs", new Object[]{time});
                    } else if (type == 4) {
                        String time = sysConfigVarCacheService.getValue("frequenty_risk_control_time");
                        resultMap.put(SUCCESS, true);
                        resultMap.put(MSG, "order.frequenty_risk_control");
                        resultMap.put("objs", new Object[]{time});
                    } else {
                        resultMap.put(SUCCESS, true);
                        resultMap.put(MSG, "order.this_action_disabled");
                    }
                } else {
                    resultMap.put(SUCCESS, true);
                    resultMap.put(MSG, "order.this_action_disabled");
                }
            } else {
                resultMap.put(SUCCESS, true);
                resultMap.put(MSG, "account.normal");
            }
        } else {
            resultMap.put(SUCCESS, false);
            resultMap.put(MSG, "user.account_not_exist");
        }
        return resultMap;
    }

    @Override
    public List<SelectRootUserIdsResponse> selectRootUserIds(SelectRootUserRequest body)throws Exception{
        if (body == null || CollectionUtils.isEmpty(body.getUserIds()) || body.getUserIds().size() > 501){
            return Lists.newArrayList();
        }
        List<Long> userIds = body.getUserIds();
        Integer resType = body.getResType();
        List<RootUserIndex> rootUserIndexs =rootUserIndexMapper.selectByUserIds(userIds);
        if (resType == null || resType == 0 || body.getParentUserId() == null){
            return toRootUserIdsRes(rootUserIndexs);
        }else{
            Map<String,Object> param = new HashMap<>();
            param.put("parentUserId", body.getParentUserId());
            Map<Long, Long> userRootMap = rootUserIndexs.stream().collect(Collectors.toMap(RootUserIndex::getRootUserId, RootUserIndex::getUserId, (k1, k2) -> k2));
            param.put("userIds", new ArrayList<>(userRootMap.keySet()));
            List<SubUserBinding> subUserBindings = subUserBindingMapper.selectByParentUserIdAndSubUserIds(param);
            return SubBindstoRootUserIdsRes(subUserBindings,userRootMap);
        }
    }

    private List<SelectRootUserIdsResponse> SubBindstoRootUserIdsRes(List<SubUserBinding> subUserBindings,Map<Long, Long> userRootMap) {
        if (CollectionUtils.isEmpty(subUserBindings) && subUserBindings.size() == 0){
            return Lists.newArrayList();
        }
        List<SelectRootUserIdsResponse> resList =  new ArrayList<>(subUserBindings.size());
        for (SubUserBinding binding:subUserBindings){
            SelectRootUserIdsResponse res = new SelectRootUserIdsResponse();
            res.setRootUserId(binding.getSubUserId());
            if (userRootMap.containsKey(binding.getSubUserId())){
                res.setUserId(userRootMap.get(binding.getSubUserId()));
            }
            res.setBrokerSubAcountId(binding.getBrokerSubAccountId());
            resList.add(res);
        }
        return resList;
    }

    private List<SelectRootUserIdsResponse> toRootUserIdsRes(List<RootUserIndex> rootUserIndexs) {
        if (CollectionUtils.isEmpty(rootUserIndexs) || rootUserIndexs.size() == 0){
            return Lists.newArrayList();
        }
        List<SelectRootUserIdsResponse> resList =  new ArrayList<>(rootUserIndexs.size());
        for (RootUserIndex index:rootUserIndexs){
            SelectRootUserIdsResponse res = new SelectRootUserIdsResponse();
            res.setRootUserId(index.getRootUserId());
            res.setUserId(index.getUserId());
            resList.add(res);
        }
        return resList;
    }


    @Override
    public SelectUserRiskMessage selectUserMessgeForRisk(Long userId) {
        User user = userCommonBusiness.checkAndGetUserById(userId);
        SelectUserRiskMessage riskMessage = new SelectUserRiskMessage();
        riskMessage.setUserId(String.valueOf(userId));
        riskMessage.setUserEmail(user.getEmail());
        UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
        if (userKycApprove != null) {
            StringBuffer name = new StringBuffer()
                    .append((StringUtils.isBlank(userKycApprove.getCertificateFirstName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateFirstName())) ? ""
                            : userKycApprove.getCertificateFirstName().trim().replaceAll("N/A", ""))
                    .append((StringUtils.isBlank(userKycApprove.getCertificateLastName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateLastName())) ? ""
                            : " " + userKycApprove.getCertificateLastName().trim().replaceAll("N/A", ""));
            riskMessage.setName(name.toString());
        }
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity != null) {
            String mobile = userSecurity.getMobile();
            String mobileCode = userSecurity.getMobileCode();
            if (StringUtils.isNotBlank(mobileCode)) {
                Country country = countryMapper.selectByPrimaryKey(mobileCode.toUpperCase());
                riskMessage.setPhone("+" + country.getMobileCode() + userSecurity.getMobile());
            }
        }
        UserDevice userDevice = userDeviceMapper.selectUserLastLoginDevice(userId);
        if (userDevice != null){
            riskMessage.setDeviceInfo(userDevice.getContent());
        }
        UserSecurityLog lastLoginLogByUserId = userSecurityLogMapper.getLastLoginLogByUserId(userId);
        if (lastLoginLogByUserId != null){
            riskMessage.setIp(lastLoginLogByUserId.getIp());
        }
        UserSecurityLog lastUpdatePwdByUserId = userSecurityLogMapper.getLastUpdatePwdByUserId(userId);
        if (lastUpdatePwdByUserId != null){
            riskMessage.setChangedPassword(true);
        }
        return riskMessage;
    }




}
