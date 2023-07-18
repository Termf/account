package com.binance.account.service.subuser.impl;

import com.binance.account.common.constant.UserDeviceConst;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.tradelevel.TradeLevel;
import com.binance.account.data.mapper.tradelevel.TradeLevelMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.utils.JsonUtils;
import com.binance.matchbox.vo.TradingAccountDetails;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.constant.UserDeviceConst;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.subuser.SubUserBindingDelete;
import com.binance.account.data.entity.tradelevel.TradeLevel;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.mapper.certificate.UserAddressMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingDeleteMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.tradelevel.TradeLevelMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.subuser.ParentUserBaseDetailsVo;
import com.binance.account.vo.subuser.SubUserBaseDetailsVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.response.BaseDetailResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.CopyBeanUtils;
import com.binance.master.utils.CouplingCalculationUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.TradingAccountDetails;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fei.Huang on 2018/10/10.
 */
@Log4j2
public class BaseSubUserBusiness {

    protected static final String DEFAULT_RESULT = "lctwmv9fdld6yfdk06g";

    @Resource
    protected AccountApi accountApi;
    @Resource
    protected UserMapper userMapper;
    @Resource
    protected ISysConfig iSysConfig;
    @Resource
    protected UserIpMapper userIpMapper;
    @Resource
    protected UserInfoMapper userInfoMapper;
    @Autowired
    protected IUserDevice userDeviceBusiness;
    @Resource
    protected UserIndexMapper userIndexMapper;
    @Resource
    protected IMsgNotification iMsgNotification;
    @Resource
    protected UserAddressMapper userAddressMapper;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Resource
    protected UserSecurityMapper userSecurityMapper;
    @Autowired
    protected SubUserBindingMapper subUserBindingMapper;
    @Resource
    protected UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    protected UserMobileIndexMapper userMobileIndexMapper;
    @Autowired
    private UserCommonValidateService userCommonValidateService;

    @Autowired
    private TradeLevelMapper tradeLevelMapper;

    @Autowired
    protected SubUserBindingDeleteMapper subUserBindingDeleteMapper;

    /**
     * 子账号BaseDetails
     *
     * @param subUserId
     * @return
     */
    protected SubUserBaseDetailsVo getSubUserBaseDetails(final Long subUserId, final String remark,
            final Date bindingTime, final Date createTime,Long brokerSubAccountId) {
        BaseDetailResponse baseDetailsVo = getBaseDetailResponse(subUserId);
        SubUserBaseDetailsVo subUserBaseDetailsVo = CopyBeanUtils.copy(baseDetailsVo, SubUserBaseDetailsVo.class);
        subUserBaseDetailsVo.setSubUserId(subUserId);
        subUserBaseDetailsVo.setRemark(remark);
        subUserBaseDetailsVo.setBindingTime(bindingTime);
        subUserBaseDetailsVo.setCreateTime(createTime);
        subUserBaseDetailsVo.setBrokerSubAccountId(brokerSubAccountId);
        return subUserBaseDetailsVo;
    }

    /**
     * 母账号BaseDetails
     *
     * @param parentUserId
     * @param createTime
     * @return
     */
    protected ParentUserBaseDetailsVo getParentUserBaseDetails(final Long parentUserId, final Date createTime) {
        BaseDetailResponse baseDetailsVo = getBaseDetailResponse(parentUserId);
        ParentUserBaseDetailsVo parentUserBaseDetailsVo =
                CopyBeanUtils.copy(baseDetailsVo, ParentUserBaseDetailsVo.class);
        parentUserBaseDetailsVo.setParentUserId(parentUserId);
        parentUserBaseDetailsVo.setCreateTime(createTime);

        long subUserCount = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);
        parentUserBaseDetailsVo.setSubUserCount(subUserCount);

        return parentUserBaseDetailsVo;
    }

    private BaseDetailResponse getBaseDetailResponse(final Long userId) {
        BaseDetailResponse baseDetailsVo = new BaseDetailResponse();

        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user;
        // 强制从主库读，避免读写延迟问题，确保页面展示正确的用户状态
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            user = userMapper.queryByEmail(userIndex.getEmail());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        baseDetailsVo.setEmail(userIndex.getEmail().trim().toLowerCase());
        baseDetailsVo.setStatus(user.getStatus());
        baseDetailsVo.setUserStatusEx(new UserStatusEx(user.getStatus()));

        // UserInfo
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (null != userInfo) {
            baseDetailsVo.setAgentId(userInfo.getAgentId());
            baseDetailsVo.setAgentRewardRatio(userInfo.getAgentRewardRatio());
            baseDetailsVo.setMakerCommission(userInfo.getMakerCommission());
            baseDetailsVo.setTakerCommission(userInfo.getTakerCommission());
            baseDetailsVo.setBuyerCommission(userInfo.getBuyerCommission());
            baseDetailsVo.setSellerCommission(userInfo.getSellerCommission());
            baseDetailsVo.setDailyWithdrawCap(userInfo.getDailyWithdrawCap());
            baseDetailsVo.setTradeLevel(userInfo.getTradeLevel());
        }

        // User Security
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (null != userSecurity) {
            baseDetailsVo.setMobile(userSecurity.getMobile());
            baseDetailsVo.setMobileCode(userSecurity.getMobileCode());
            baseDetailsVo.setSecurityLevel(userSecurity.getSecurityLevel());
        }

        // User Security Log
        UserSecurityLog userSecurityLog = userSecurityLogMapper.getLastLoginLogByUserId(userId);
        if (null != userSecurityLog) {
            UserSecurityLogVo userSecurityLogVo = new UserSecurityLogVo();
            BeanUtils.copyProperties(userSecurityLog, userSecurityLogVo);
            baseDetailsVo.setLastUserSecurityLog(userSecurityLogVo);
        }

        // KYC
        KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);
        baseDetailsVo.setCertificateType(certificateResult.getCertificateType());
        baseDetailsVo.setCertificateMessage(certificateResult.getCertificateMessage());
        baseDetailsVo.setCertificateStatus(certificateResult.getCertificateStatus());
        baseDetailsVo.setFirstName(certificateResult.getFirstName());
        baseDetailsVo.setLastName(certificateResult.getLastName());
        baseDetailsVo.setCompanyName(certificateResult.getCompanyName());

        // User Address
        SysConfig addressVerificationConfig = iSysConfig.selectByDisplayName("address_verification_switch");
        if (addressVerificationConfig != null && "ON".equalsIgnoreCase(addressVerificationConfig.getCode())) {
            UserAddress userAddress = userAddressMapper.getLast(user.getUserId(), UserAddress.Status.PASSED.ordinal());
            if (null != userAddress) {
                baseDetailsVo.setCertificateAddress(userAddress.getFullAddress());
            }
        }
        return baseDetailsVo;
    }

    /**
     * 主账号绑定子账号
     *
     * @param parentUserId
     * @param subUserId
     * @param remark
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected int createParentSubUserBinding(final Long parentUserId, final Long subUserId, final String remark) {
        SubUserBinding subUserBinding = new SubUserBinding();
        subUserBinding.setParentUserId(parentUserId);
        subUserBinding.setSubUserId(subUserId);
        subUserBinding.setRemark(StringUtils.defaultString(remark));
        return subUserBindingMapper.insert(subUserBinding);
    }


    /**
     * 存入delete表
     *
     * @param parentUserId
     * @param subUserId
     * @param remark
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected int createParentSubUserBindingDelete(final Long parentUserId, final Long subUserId, final String remark,final Long brokerSubAccountId) {
        SubUserBindingDelete subUserBindingDelete = new SubUserBindingDelete();
        subUserBindingDelete.setParentUserId(parentUserId);
        subUserBindingDelete.setSubUserId(subUserId);
        subUserBindingDelete.setRemark(StringUtils.defaultString(remark));
        subUserBindingDelete.setBrokerSubAccountId(brokerSubAccountId);
        return subUserBindingDeleteMapper.insertSelective(subUserBindingDelete);
    }

    /**
     * 删除子账号、主账号绑定关系
     *
     * @param subUserId
     */
    protected int deleteParentSubUserBinding(Long parentUserId, Long subUserId) {
        return subUserBindingMapper.deleteBySubUserIdAndParentUserId(parentUserId,subUserId);
    }

    /**
     * 创建用户
     *
     * @param email
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected User createUser(final String email, final String password, final boolean isSubUser)
            throws NoSuchAlgorithmException {
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, password, cipherCode);
        // 若为子账号，则关闭子账号功能、标记成子账号
        if (isSubUser) {
            Long status = user.getStatus();
            status = BitUtils.disable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
            status = BitUtils.enable(BitUtils.enable(status, Constant.USER_IS_SUBUSER),
                    Constant.USER_IS_SUB_USER_ENABLED);
            user.setStatus(status);
        }
        userMapper.insert(user);
        return user;
    }


    /**
     * 创建无邮箱用户
     *
     * @param email
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createNoEmailSubUser(final String email, final String password)
            throws NoSuchAlgorithmException {
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, password, cipherCode);
        Long status = user.getStatus();
        status = BitUtils.disable(status, AccountCommonConstant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        status = BitUtils.disable(status, AccountCommonConstant.USER_LOGIN);
        status = BitUtils.enable(BitUtils.enable(status, AccountCommonConstant.USER_IS_SUBUSER), AccountCommonConstant.USER_IS_SUB_USER_ENABLED);
        status = BitUtils.enable(status, AccountCommonConstant.USER_IS_NO_EMAIL_SUB_USER);//是否是NoEmailSubUser
        user.setStatus(status);
        userMapper.insert(user);
        return user;
    }

    /**
     * 创建用户Security信息
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createUserSecurity(final Long userId, final String userEmail) {
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
     * 创建用户信息
     *
     * @param subUserId
     * @param agentId
     * @param trackSource
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createSubUserInfo(final Long parentUserId, final Long subUserId, final Long agentId,
            final String trackSource) {

        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(parentUserId);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(subUserId);
        userInfo.setParent(parentUserInfo.getUserId());
        // 经纪人返佣比例
        userInfo.setAgentRewardRatio(parentUserInfo.getAgentRewardRatio());
        //被推荐人返佣
        userInfo.setReferralRewardRatio(parentUserInfo.getReferralRewardRatio());
        // 用户交易账号 激活时创建
        userInfo.setTradingAccount(null);
        // 被动方手续费
        userInfo.setMakerCommission(parentUserInfo.getMakerCommission());
        // 主动方手续费
        userInfo.setTakerCommission(parentUserInfo.getTakerCommission());
        // 买方交易手续费
        userInfo.setBuyerCommission(parentUserInfo.getBuyerCommission());
        // 卖方交易手续费
        userInfo.setSellerCommission(parentUserInfo.getSellerCommission());
        // 单日最大出金总金额
        userInfo.setDailyWithdrawCap(parentUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        userInfo.setDailyWithdrawCountLimit(parentUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        userInfo.setAutoWithdrawAuditThreshold(parentUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        userInfo.setTradeLevel(parentUserInfo.getTradeLevel());
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(parentUserInfo.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        // 推荐人
        userInfo.setAgentId(parentUserInfo.getAgentId());
        userInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.NORMAL.name());
        userInfo.setReferralRewardRatio(parentUserInfo.getReferralRewardRatio());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }
        if (userInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insertSelective(userInfo);
        return userInfo;
    }



    /**
     * 创建broker用户信息
     *
     * @param subUserId
     * @param agentId
     * @param trackSource
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createBrokerSubUserInfo(final Long parentUserId, final Long subUserId, final Long agentId,
                                     final String trackSource) {

        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(parentUserId);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(subUserId);
        userInfo.setParent(parentUserInfo.getUserId());
        // 经纪人返佣比例
        userInfo.setAgentRewardRatio(parentUserInfo.getAgentRewardRatio());
        //被推荐人返佣
        userInfo.setReferralRewardRatio(parentUserInfo.getReferralRewardRatio());
        // 用户交易账号 激活时创建
        userInfo.setTradingAccount(null);

        TradeLevel level = tradeLevelMapper.selectByLevel(0);
        if (level == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        userInfo.setMakerCommission(level.getMakerCommission());// 被动方手续费
        userInfo.setTakerCommission(level.getTakerCommission());// 主动方手续费
        userInfo.setBuyerCommission(level.getBuyerCommission());// 买方交易手续费
        userInfo.setSellerCommission(level.getSellerCommission());// 卖方交易手续费
        // 单日最大出金总金额
        userInfo.setDailyWithdrawCap(new BigDecimal(0));
        // 单日最大出金次数
        userInfo.setDailyWithdrawCountLimit(0);
        // 免审核额度
        userInfo.setAutoWithdrawAuditThreshold(new BigDecimal(0));
        // 交易等级
        userInfo.setTradeLevel(0);
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(parentUserInfo.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        // 推荐人
        userInfo.setAgentId(parentUserInfo.getAgentId());

        userInfo.setReferralRewardRatio(parentUserInfo.getReferralRewardRatio());
        userInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.NORMAL.name());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }
        if (userInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insertSelective(userInfo);
    }

    /**
     * 更新用户信息
     * 
     * @param parentUserId
     * @param subUserId
     * @return
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected int updateSubUserInfo(final Long parentUserId, final Long subUserId) {

        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(parentUserId);

        UserInfo originalSubUserInfo = userInfoMapper.selectByPrimaryKey(subUserId);
        // 说明该子账户已激活(有tradingAccount).若无,则等子账户激活创建tradingAccount
        if (null != originalSubUserInfo && null != originalSubUserInfo.getTradingAccount()) {
            TradingAccountDetails tradingAccountDetails= accountApi.getDetailsByTradingAccountId(originalSubUserInfo.getTradingAccount());
            log.info("getDetailsByTradingAccountId parentUserId={},subUserId={},tradingAccountDetails={},parentUserInfo={}",parentUserId,subUserId,JsonUtils.toJsonNotNullKey(tradingAccountDetails),
                    JsonUtils.toJsonNotNullKey(parentUserInfo));
            if(null!=tradingAccountDetails){
                boolean isEqual=tradingAccountDetails.getBuyerCommission().longValue()==CouplingCalculationUtils.feeLong(parentUserInfo.getBuyerCommission())&&
                        tradingAccountDetails.getSellerCommission().longValue()==CouplingCalculationUtils.feeLong(parentUserInfo.getSellerCommission())&&
                        tradingAccountDetails.getTakerCommission().longValue()==CouplingCalculationUtils.feeLong(parentUserInfo.getTakerCommission())&&
                        tradingAccountDetails.getMakerCommission().longValue()==CouplingCalculationUtils.feeLong(parentUserInfo.getMakerCommission());
                if(!isEqual){
                    accountApi.setCommission(originalSubUserInfo.getTradingAccount(),
                            CouplingCalculationUtils.feeLong(parentUserInfo.getBuyerCommission()),
                            CouplingCalculationUtils.feeLong(parentUserInfo.getSellerCommission()),
                            CouplingCalculationUtils.feeLong(parentUserInfo.getTakerCommission()),
                            CouplingCalculationUtils.feeLong(parentUserInfo.getMakerCommission()));
                }
            }
        }

        UserInfo subUserInfo = new UserInfo();
        subUserInfo.setUserId(subUserId);
        subUserInfo.setParent(parentUserId);
        subUserInfo.setAgentRewardRatio(parentUserInfo.getAgentRewardRatio());
        subUserInfo.setMakerCommission(parentUserInfo.getMakerCommission());
        subUserInfo.setTakerCommission(parentUserInfo.getTakerCommission());
        subUserInfo.setBuyerCommission(parentUserInfo.getBuyerCommission());
        subUserInfo.setSellerCommission(parentUserInfo.getSellerCommission());
        subUserInfo.setDailyWithdrawCap(parentUserInfo.getDailyWithdrawCap());
        subUserInfo.setDailyWithdrawCountLimit(parentUserInfo.getDailyWithdrawCountLimit());
        subUserInfo.setAutoWithdrawAuditThreshold(parentUserInfo.getAutoWithdrawAuditThreshold());
        subUserInfo.setAgentId(parentUserInfo.getAgentId());
        int result = userInfoMapper.updateByPrimaryKeySelective(subUserInfo);

        try {
            // 同步数据至PNK
            Map<String, Object> dataMsg = new HashMap<>();
            dataMsg.put(UserConst.USER_ID, subUserId);
            dataMsg.put("buyerCommission", subUserInfo.getBuyerCommission());
            dataMsg.put("sellerCommission", subUserInfo.getSellerCommission());
            dataMsg.put("takerCommission", subUserInfo.getTakerCommission());
            dataMsg.put("makerCommission", subUserInfo.getMakerCommission());
            dataMsg.put("modifyReason", "子母账户绑定");
            dataMsg.put("expectedRestoreTime", "");
            MsgNotification msg =
                    new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.SET_COMMISSION, dataMsg);
            log.info("iMsgNotification setCommission:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
            this.iMsgNotification.send(msg);
        } catch (Exception e) {
            log.error("iMsgNotification.send failed:", e);
        }

        return result;
    }

    /**
     * 发送激活邮件
     *
     * @param user
     * @param terminal
     * @return
     */
    protected String[] sendActiveEmail(final User user, final TerminalEnum terminal, String customEmailLink) {
        String[] sendParams = new String[2];
        try {
            log.info("register:发送激活邮件");
            sendParams = userCommonBusiness.sendActiveCode(user, terminal, customEmailLink);
        } catch (Exception e) {
            log.error(String.format("send register email failed, email:%s, exception:", user.getEmail()), e);
        }
        return sendParams;
    }

    /**
     * 添加设备信息、IP信息、注册日志信息
     *
     * @param userId
     * @param userEmail
     * @param terminal
     * @param deviceInfo
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void addDeviceInfoAndLogs(final Long userId, final String userEmail, final TerminalEnum terminal,
            final Map<String, String> deviceInfo) {
        final String ip = WebUtils.getRequestIp();
        try {
            // 有效期
            RedisCacheUtils.increment(ip, CacheKeys.REGISTER_IP_COUNT, 1L, 24L, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("注册ip限制", e);
        }
        // 记录设备指纹信息
        String locationCity = IP2LocationUtils.getCountryCity(ip);
        String clientType = terminal.getCode();
        AddUserDeviceResponse deviceResponse = null;
        if (deviceInfo != null) {
            try {
                userDeviceBusiness.preCheck(deviceInfo, userId, clientType);
                deviceResponse = userDeviceBusiness.addDevice(
                        userId, clientType, UserDevice.Status.AUTHORIZED, UserDeviceConst.SOURCE_REGIST, deviceInfo);
            } catch (Exception e) {
                log.error("新增设备指纹出错 parentUserId:{}, deviceInfo:{}", userId, deviceInfo, e);
            }
        }
        // 添加注册日志
        try {
            final UserSecurityLog securityLog = new UserSecurityLog(userId, ip, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
            if (deviceResponse != null) {
                securityLog.touchDevice(deviceResponse.getId(), deviceResponse.getDeviceId());
            }
            UserIp userIp = new UserIp(userId, ip);
            userIpMapper.insertIgnore(userIp);
            userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add register log failed, email:%s, exception:", userEmail), e);
        }
    }

    /**
     * 发送用户注册MQ消息至PNK同步数据
     *
     * @param user
     * @param agentId
     * @param traceSource
     * @param sendParams
     */
    protected void sendRegisterMqMsg(final User user, final Long agentId, final String traceSource,
            final String[] sendParams) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", null);
        dataMsg.put("password", null);
        dataMsg.put("registerToken", null);
        dataMsg.put("code", null);
        dataMsg.put("agentId", agentId);
        dataMsg.put("trackSource", traceSource);
        dataMsg.put("ipAddress", WebUtils.getRequestIp());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.REGISTER, dataMsg);
        log.info("iMsgNotification sendRegisterMqMsg:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }


    /**
     * 发送用户注册MQ消息至PNK同步数据（针对创建no Email SubUser账户）
     *
     * @param user
     * @param userInfo
     */
    public void sendRegisterMqMsgForNoEmailSubUser(User user, UserInfo userInfo) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", "");
        dataMsg.put("code", "");
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        if (WebUtils.getHttpServletRequest() != null) {
            dataMsg.put("ipAddress", WebUtils.getRequestIp());
        }
        dataMsg.put("tradingAccount", userInfo.getTradingAccount());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.CREATE_MARGIN, dataMsg);
        log.info("iMsgNotification sendRegisterMqMsgForNoEmailSubUser:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }


    protected void sendUserProductFeeMsg(Long parentUserId,Long subUserId) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put("parentUserId", parentUserId);
        dataMsg.put("subUserId", subUserId);
        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
        log.info("iMsgNotification sendUserProductFeeMsg:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }

    /**
     * IP注册限制检查
     */
    protected void checkRegisterIpLimit() {
        long maxIpCount = Long.parseLong(iSysConfig.selectByDisplayName("max_register_count").getCode());
        long ipCount = RedisCacheUtils.get(WebUtils.getRequestIp(), Long.class, CacheKeys.REGISTER_IP_COUNT, 0L);
        if (ipCount >= maxIpCount) {
            throw new BusinessException(GeneralCode.USER_REGISTER_IP_EXCEED);
        }
    }

    protected int disableSubUser(final Long subUserId) {
        final UserIndex subUserIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        if (null == subUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User subUser = this.userMapper.queryByEmail(subUserIndex.getEmail());
        if (null == subUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User newSubUser = new User();
        newSubUser.setEmail(subUser.getEmail());
        Long status=subUser.getStatus();
        // 子账号不能拥有字母账号功能
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        // 标记为子账号
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER);
        // 标记为子账号且被母账号启用
        status = BitUtils.disable(status, Constant.USER_IS_SUB_USER_ENABLED);
        newSubUser.setStatus(status);
        int result = userMapper.updateUserStatusByEmail(newSubUser);
        log.info("updateIsSubUserEnabled done, userId:{}, result:{}", subUserId, result);
        return result;
    }

    protected int enableSubUser(final Long subUserId) {
        return updateIsSubUserEnabled(subUserId, true);
    }

    protected int updateIsSubUserEnabled(final Long subUserId, final boolean enabled) {
        final UserIndex subUserIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        if (null == subUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User subUser = this.userMapper.queryByEmail(subUserIndex.getEmail());
        if (null == subUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User newSubUser = new User();
        newSubUser.setEmail(subUser.getEmail());
        newSubUser.setStatus(enabled ? BitUtils.enable(subUser.getStatus(), Constant.USER_IS_SUB_USER_ENABLED)
                : BitUtils.disable(subUser.getStatus(), Constant.USER_IS_SUB_USER_ENABLED));
        int result = userMapper.updateUserStatusByEmail(newSubUser);
        log.info("updateIsSubUserEnabled done, userId:{}, enabled:{}, result:{}", subUserId, enabled, result);
        return result;
    }



    protected int disableBrokerSubUser(final Long subUserId) {
        return updateIsBrokerSubUserEnabled(subUserId, false);
    }

    protected int enableBrokerSubUser(final Long subUserId) {
        return updateIsBrokerSubUserEnabled(subUserId, true);
    }

    protected int updateIsBrokerSubUserEnabled(final Long subUserId, final boolean enabled) {
        final UserIndex subUserIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        if (null == subUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User subUser = this.userMapper.queryByEmail(subUserIndex.getEmail());
        if (null == subUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User newSubUser = new User();
        newSubUser.setEmail(subUser.getEmail());
        Long subsuerStatus=subUser.getStatus();
        if(enabled){
            subsuerStatus=BitUtils.enable(subsuerStatus, Constant.USER_IS_BROKER_SUB_USER_ENABLED);
            subsuerStatus=BitUtils.enable(subsuerStatus, Constant.USER_IS_BROKER_SUBUSER);
        }else{
            subsuerStatus=BitUtils.disable(subsuerStatus, Constant.USER_IS_BROKER_SUB_USER_ENABLED);
        }
        newSubUser.setStatus(subsuerStatus);
        int result = userMapper.updateUserStatusByEmail(newSubUser);
        log.info("updateIsBrokerSubUserEnabled done, userId:{}, enabled:{}, result:{},subsuerStatus:{}", subUserId, enabled, result,subsuerStatus);
        return result;
    }


    protected int rollbackBrokerSubuer(final Long subUserId) {
        final UserIndex subUserIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        if (null == subUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User subUser = this.userMapper.queryByEmail(subUserIndex.getEmail());
        if (null == subUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User newSubUser = new User();
        newSubUser.setEmail(subUser.getEmail());
        Long status=subUser.getStatus();
        status=BitUtils.disable(status, Constant.USER_IS_BROKER_SUB_USER_ENABLED);
        status=BitUtils.disable(status, Constant.USER_IS_BROKER_SUBUSER);
        newSubUser.setStatus(status);
        int result = userMapper.updateUserStatusByEmail(newSubUser);
        log.info("rollbackBrokerSubuer done, userId:{},status={}, result:{}", subUserId, status,result);
        return result;
    }



}
