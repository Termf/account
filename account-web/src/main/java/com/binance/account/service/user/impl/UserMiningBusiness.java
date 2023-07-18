package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.service.user.IUserMining;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.vo.mining.request.CreateMingAccountRequest;
import com.binance.account.vo.mining.response.CreateMiningUserResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.matchbox.api.AccountApi;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

import static com.binance.account.service.user.impl.UserBusiness.DEFAULT_RESULT;


@Log4j2
@Service
@Monitored
public class UserMiningBusiness implements IUserMining {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserTradingAccountMapper userTradingAccountMapper;
    @Autowired
    private UserCommonValidateService userCommonValidateService;
    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private UserAgentLogMapper userAgentLogMapper;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private ISysConfig iSysConfig;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected UserSecurityMapper userSecurityMapper;
    @Autowired
    protected UserSecurityLogMapper userSecurityLogMapper;
    @Autowired
    private AccountApi accountApi;
    @Autowired
    private SubUserBindingMapper subUserBindingMapper;



    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<CreateMiningUserResponse> createMiningAccount(APIRequest<CreateMingAccountRequest> request) throws Exception {
        final CreateMingAccountRequest requestBody = request.getBody();
        log.info("Get creating mining account request with uid-{}", requestBody.getUserId());
        //校验并且获取主账户信息  ,rootuser这里解释为主账户
        Pair<User, UserInfo> rootTuple = checkAndGetUserByIdForMiningVersion(requestBody.getUserId(),null);
        //获取主账号相关信息
        User rootUser = rootTuple.getLeft();
        UserInfo rootUserInfo = rootTuple.getRight();
        if(null!=rootUserInfo.getMiningUserId()){
            CreateMiningUserResponse createMiningUserResponse = new CreateMiningUserResponse();
            createMiningUserResponse.setRootUserId(rootUserInfo.getUserId());
            createMiningUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
            UserInfo oldMininigUserInfo = userInfoMapper.selectByPrimaryKey(rootUserInfo.getMiningUserId());
            if (null == oldMininigUserInfo) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            createMiningUserResponse.setMiningUserId(oldMininigUserInfo.getUserId());
            createMiningUserResponse.setMiningTradingAccount(oldMininigUserInfo.getTradingAccount());
            log.info("old mininig account for uid-{} succeed with result-{}", requestBody.getUserId(), createMiningUserResponse);
            return APIResponse.getOKJsonResult(createMiningUserResponse);
        }

        //开始创建mining账号相关信息
        String vritualEmail = createVirtualEmailForMining(rootUser.getEmail(), rootUser.getUserId());
        User miningUser = ((UserMiningBusiness) AopContext.currentProxy()).createMiningUser(vritualEmail);

        // 创建mining账号Security信息
        ((UserMiningBusiness) AopContext.currentProxy()).createMiningUserSecurity(miningUser.getUserId(), miningUser.getEmail());

        // 创建mining账号info信息
        UserInfo miningUserInfo = ((UserMiningBusiness) AopContext.currentProxy()).createMingUserInfo(rootUserInfo, miningUser.getUserId());

        // 更新root表中的mininguserid （这步操作是幂等的）
        rootUserInfo.setMiningUserId(miningUser.getUserId());
        userInfoMapper.updateByPrimaryKeySelective(rootUserInfo);

        //更新主账户的状态(这步操作也是幂等的),这是用来标明这个账户是否拥有margin账户
        rootUser.setStatus(BitUtils.enable(rootUser.getStatus(), AccountCommonConstant.USER_IS_EXIST_MINING_ACCOUNT));
        userMapper.updateByEmailSelective(rootUser);
        // 创建mining交易账户
        //这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long miningTradingAccount = matchboxApiClient.postAccount(miningUserInfo, MatchBoxAccountTypeEnum.C2C);
        miningUserInfo.setTradingAccount(miningTradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(miningUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(miningTradingAccount);
        userTradingAccount.setUserId(miningUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        log.info("UserMiningBusiness.postAccount insert:{}", JSON.toJSONString(userTradingAccount));
        //禁用交易
        // 2.交易禁用-调用撮合引擎
        if (null != miningUserInfo && null != miningUserInfo.getTradingAccount() && miningUserInfo.getTradingAccount() > 0) {
            accountApi.setTradingAccount(miningUserInfo.getTradingAccount(), false, true, true);
            log.info("UserMiningBusiness.disable tradingAccount done, userId:{}, tradingAccountId:{}", miningUserInfo.getUserId(),
                    miningUserInfo.getTradingAccount());
        }
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        if(isSubUser){
            SubUserBinding updateSubUserBinding=new SubUserBinding();
            updateSubUserBinding.setParentUserId(rootUserInfo.getParent());
            updateSubUserBinding.setSubUserId(rootUserInfo.getUserId());
            updateSubUserBinding.setMiningUserId(miningUser.getUserId());
            log.info("updateSelectiveBySubUserIdAndParentUserId={}",JsonUtils.toJsonNotNullKey(updateSubUserBinding));
            subUserBindingMapper.updateSelectiveBySubUserIdAndParentUserId(updateSubUserBinding);
        }


        AsyncTaskExecutor.execute(() -> {
            try {
                // 加入推荐记录表
                insertToAgentLog(requestBody.getUserId(), miningUserInfo.getUserId(), miningUser.getEmail());
            } catch (Exception e) {
                log.error("insertToAgentLog exception", e);
            }
            try {
                userCommonBusiness.insertInfoRootUserIndex(requestBody.getUserId(),miningUser.getUserId(), UserTypeEnum.MINING.name());
            }catch (Exception e){
                log.error("insertInfoRootUserIndex minging exception", e);
            }
        });
        CreateMiningUserResponse createMiningUserResponse = new CreateMiningUserResponse();
        createMiningUserResponse.setRootUserId(rootUserInfo.getUserId());
        createMiningUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createMiningUserResponse.setMiningUserId(miningUserInfo.getUserId());
        createMiningUserResponse.setMiningTradingAccount(miningTradingAccount);

        log.info("Create mininig account for uid-{} succeed with result-{}", requestBody.getUserId(), createMiningUserResponse);

        return APIResponse.getOKJsonResult(createMiningUserResponse);
    }




    /**
     * 校验用户信息（Mining version）
     *
     * @return Pair 返回的是一个元组，主要是不想再单独包个对象了，为了简单
     */
    protected Pair<User, UserInfo> checkAndGetUserByIdForMiningVersion(Long userId,Long parentUserId) throws Exception {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User rootUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == rootUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //检查传进来的userid是否是future的userid
        //当前账号不能是future 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_FUTURE_USER)) {
            throw new BusinessException(AccountErrorCode.FUTURE_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }

        //当前账号没有激活
        if (!BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_ACTIVE)) {
            throw new BusinessException(AccountErrorCode.ACTIVE_FUTURE_ACCOUNT_FAILED);
        }
        //当前账号不能是margin 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_MARGIN_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }

        //当前账号不能是fiat 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_FIAT_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }

        //当前账号不能是mining 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_MINING_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }
        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null == rootUserInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Pair<User, UserInfo> twoTuple = Pair.of(rootUser, rootUserInfo);
        return twoTuple;
    }

    /**
     * 创建一个虚拟邮箱（幂等）
     */
    protected String createVirtualEmailForMining(String email, Long userId) {
        String[] emailArray = email.split("@");
        String virtualEmail = emailArray[0] + "_" + String.valueOf(userId)+ "_mining@" + emailArray[1];
        return virtualEmail;
    }

    /**
     * 创建用户（不幂等，但是会回滚）
     *
     * @param vritualEmail
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createMiningUser(final String vritualEmail) throws NoSuchAlgorithmException {
        //这里加了事务回滚，所以如果报错数据直接回滚
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(vritualEmail);
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, "123456", cipherCode);
        //实际上mining账号的user并不需要密码和salt所以设置为空字符串
        user.setPassword("");
        user.setSalt("");
        //因为是minig账号所以只有交易功能，还有需要标志成future
        Long status = user.getStatus();
        status = BitUtils.enable(status, AccountCommonConstant.USER_IS_MINING_USER);
        //禁止登录
        status = BitUtils.enable(status, AccountCommonConstant.USER_LOGIN);
        // 交易禁用
        status = BitUtils.enable(status, AccountCommonConstant.USER_TRADE);
        user.setStatus(status);
        userMapper.insert(user);
        return user;
    }


    /**
     * 创建Minig用户Security信息（不幂等，但是会回滚）
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createMiningUserSecurity(final Long userId, final String userEmail) {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(userId);
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");
        userSecurity.setSecurityLevel(1);
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(1);
        userSecurity.setWithdrawSecurityAutoStatus(1);
        userSecurityMapper.insert(userSecurity);
    }

    /**
     * 创建Mining用户信息（不幂等，但是会回滚）
     *
     * @param rootUserInfo
     * @param miningUserId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createMingUserInfo(UserInfo rootUserInfo, Long miningUserId) {
        //逻辑很简单，从主账号的userinfo把信息都copy过来就完事了
        UserInfo miningUserInfo = new UserInfo();
        miningUserInfo.setUserId(miningUserId);
        // 被推荐人返佣比例
        miningUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        // 经纪人返佣比例
        miningUserInfo.setAgentRewardRatio(rootUserInfo.getAgentRewardRatio());
        // 用户交易账号 激活时创建
        miningUserInfo.setTradingAccount(null);
        // 被动方手续费
        miningUserInfo.setMakerCommission(rootUserInfo.getMakerCommission());
        // 主动方手续费
        miningUserInfo.setTakerCommission(rootUserInfo.getTakerCommission());
        // 买方交易手续费
        miningUserInfo.setBuyerCommission(rootUserInfo.getBuyerCommission());
        // 卖方交易手续费
        miningUserInfo.setSellerCommission(rootUserInfo.getSellerCommission());
        // 单日最大出金总金额
        miningUserInfo.setDailyWithdrawCap(rootUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        miningUserInfo.setDailyWithdrawCountLimit(rootUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        miningUserInfo.setAutoWithdrawAuditThreshold(rootUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        miningUserInfo.setTradeLevel(rootUserInfo.getTradeLevel());
        // 新返佣比例
        miningUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        miningUserInfo.setNickName("");
        miningUserInfo.setRemark("");
        miningUserInfo.setTrackSource(rootUserInfo.getTrackSource());
        miningUserInfo.setInsertTime(DateUtils.getNewDate());
        miningUserInfo.setUpdateTime(DateUtils.getNewDate());
        // 推荐人
        miningUserInfo.setAgentId(rootUserInfo.getAgentId());
        miningUserInfo.setAccountType(UserTypeEnum.MINING.name());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            miningUserInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId = userCommonValidateService.isValidateAgentId(rootUserInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            miningUserInfo.setAgentId(null);
        }
        if (rootUserInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            miningUserInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insert(miningUserInfo);
        return miningUserInfo;
    }



    private void insertToAgentLog(Long parentUserId, Long currentUserId, String currentEmail) {
        UserAgentLog existParent = userAgentLogMapper.selectByReferralUserId(parentUserId);
        if (existParent == null) {
            return;
        }
        UserAgentLog userAgentLog = new UserAgentLog();
        userAgentLog.setAgentCode(existParent.getAgentCode());
        userAgentLog.setUserId(existParent.getUserId());
        userAgentLog.setReferralUser(currentUserId);
        userAgentLog.setReferralEmail(currentEmail);
        User user = userMapper.queryByEmail(currentEmail);
        if (user != null){
            userAgentLog.setUserType(UserTypeEnum.getAccountType(user.getStatus()));
        }
        userAgentLogMapper.insertSelective(userAgentLog);
    }






}
