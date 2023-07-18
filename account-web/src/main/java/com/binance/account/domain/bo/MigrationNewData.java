package com.binance.account.domain.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.UserCertificate;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.master.constant.Constant;
import com.binance.master.old.models.account.OldCompanyAuthentication;
import com.binance.master.old.models.account.OldUser;
import com.binance.master.old.models.account.OldUserData;
import com.binance.master.old.models.account.OldUserIdPhoto;
import com.binance.master.old.models.account.OldUserIpKey;
import com.binance.master.old.models.account.OldUserLog;
import com.binance.master.old.models.account.OldUserSecurity;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Fei.Huang on 2018/4/10.
 */
@Getter
@Setter
public class MigrationNewData {

    private MigrationOldData migrationOldData;

    private int maxErrorCount;

    private OldUser oldUser;

    private User user;
    private UserInfo userInfo;
    private List<UserIp> userIps;
    private UserSecurity userSecurity;
    private List<UserSecurityLog> userSecurityLogs;
    private UserCertificate userCertificate;
    private CompanyCertificate companyCertificate;
    private List<UserOperationLog> userOperationLogs;

    public MigrationNewData(OldUser oldUser, MigrationOldData migrationOldData, int maxErrorCount) throws Exception {
        this.maxErrorCount = maxErrorCount;

        OldUserData oldUserData = migrationOldData.getOldUserDataMap().get(oldUser.getUserId());
        if (null == oldUserData) {
            oldUserData = new OldUserData();
            oldUserData.setUserId(oldUser.getUserId());
        }
        OldUserSecurity oldUserSecurity = migrationOldData.getOldUserSecurityMap().get(oldUser.getUserId());
        if (null == oldUserSecurity) {
            oldUserSecurity = new OldUserSecurity();
            oldUserSecurity.setUserId(oldUser.getUserId());
        }
        List<OldUserIpKey> oldUserIpKeyList = migrationOldData.getOldUserIpKeysMap().get(oldUser.getUserId());
        OldUserIdPhoto oldUserIdPhoto = migrationOldData.getOldUserIdPhotoMap().get(oldUser.getUserId());
        OldCompanyAuthentication oldCompAuth =
                migrationOldData.getOldCompanyAuthenticationMap().get(oldUser.getUserId());
        List<OldUserLog> oldUserLogList = migrationOldData.getOldUserLogMap().get(oldUser.getUserId());

        this.user = convertUser(oldUserData, oldUserSecurity, oldUser);
        this.userInfo = convertUserInfo(oldUserData, user, oldUser);
        this.userIps = convertUserIps(oldUserIpKeyList);
        this.userSecurity = convertUserSecurity(oldUserData, oldUserSecurity, user, oldUser);
        this.userSecurityLogs = convertUserSecurityLogs(oldUserLogList);
        this.userCertificate = convertUserCertificate(oldUserIdPhoto, user, userSecurity);
        //this.companyCertificate = convertCompanyCertificate(oldCompAuth, user, userSecurity);
    }

    /**
     * 转换User
     *
     * @param oldUserDataTemp
     * @param oldUserSecurityTemp
     * @param oldUser
     * @return
     */
    private User convertUser(OldUserData oldUserDataTemp, OldUserSecurity oldUserSecurityTemp, OldUser oldUser) {
        User user = new User();
        user.setUserId(Long.valueOf(oldUser.getUserId()));
        user.setEmail(oldUser.getEmail());
        user.setPassword(oldUser.getPassword());
        user.setSalt(oldUser.getSalt());
        user.setInsertTime(oldUser.getCreateTime());


        Date updateTime = oldUser.getUpdateTime();
        if (null == updateTime) {
            updateTime = DateUtils.getNewUTCDate();
        }
        user.setUpdateTime(updateTime);

        Long userStatus = 0L;
        if (StringUtils.equals("1", oldUser.getEmailVerified())) {// 激活
            userStatus = BitUtils.enable(userStatus, Constant.USER_ACTIVE);
        }
        if (StringUtils.equals("disable", oldUserSecurityTemp.getStatus())) {// 禁用
            userStatus = BitUtils.enable(userStatus, Constant.USER_DISABLED);
        }
        if (StringUtils.equals("lock", oldUserSecurityTemp.getStatus())) {// 锁定
            userStatus = BitUtils.enable(userStatus, Constant.USER_LOCK);
        }
        if (oldUserDataTemp.getSpecialFlag() != null && oldUserDataTemp.getSpecialFlag()) {// 特殊用户
            userStatus = BitUtils.enable(userStatus, Constant.USER_SPECIAL);
        }
        if (oldUserDataTemp.getSeedUser() != null && 1 == oldUserDataTemp.getSeedUser().intValue()) {// 种子用户
            userStatus = BitUtils.enable(userStatus, Constant.USER_SEND);
        }
        if (oldUserSecurityTemp.getConfirmTips() != null && oldUserSecurityTemp.getConfirmTips()) {// 协议确认
            userStatus = BitUtils.enable(userStatus, Constant.USER_PROTOCOL);
        }
        if (oldUserSecurityTemp.getMobileSecurity() != null
                && 1 == oldUserSecurityTemp.getMobileSecurity().intValue()) {// 是否需要手机验证
            userStatus = BitUtils.enable(userStatus, Constant.USER_MOBILE);
        }
        if (oldUserDataTemp.getChangePass() != null && oldUserDataTemp.getChangePass()) {// 强制修改密码
            userStatus = BitUtils.enable(userStatus, Constant.USER_FORCED_PASSWORD);
        }
        if (oldUserDataTemp.getPurchase() != null && !oldUserDataTemp.getPurchase()) {// 申购
            userStatus = BitUtils.enable(userStatus, Constant.USER_PURCHASE);
        }
        if (oldUserDataTemp.getTradeForbidden() != null
                && oldUserDataTemp.getTradeForbidden().byteValue() == ((byte) 1)) {// 交易
            userStatus = BitUtils.enable(userStatus, Constant.USER_TRADE);
        }
        if (oldUserDataTemp.getAppTrade() != null && ((byte) 1) == oldUserDataTemp.getAppTrade().byteValue()) {// app交易
            userStatus = BitUtils.enable(userStatus, Constant.USER_TRADE_APP);
        }
        if (oldUserDataTemp.getApiTradeStatus() != null && !oldUserDataTemp.getApiTradeStatus()) {// api交易
            userStatus = BitUtils.enable(userStatus, Constant.USER_TRADE_API);
        }
        if (oldUserDataTemp.getCommissionStatus() != null && 1 == oldUserDataTemp.getCommissionStatus().intValue()) {// BNB手续费开关
            userStatus = BitUtils.enable(userStatus, Constant.USER_FEE);
        }
//        if (StringUtils.isNotBlank(oldUserSecurityTemp.getSecretKey())) {// 谷歌2次验证
        if (StringUtils.isNotBlank(oldUserSecurityTemp.getEncryptedSecretKey())) {// 谷歌2次验证
            userStatus = BitUtils.enable(userStatus, Constant.USER_GOOGLE);
        }
        if (oldUserSecurityTemp.getWithdrawWhiteStatus() != null && oldUserSecurityTemp.getWithdrawWhiteStatus()) {// 是否开启出入金地址白名单
            userStatus = BitUtils.enable(userStatus, Constant.USER_WITHDRAW_WHITE);
        }
//        if (null != oldUserDataTemp.getChildren() && oldUserDataTemp.getChildren().byteValue() == ((byte) 1)) {// 子账号
//            userStatus = BitUtils.enable(userStatus, Constant.USER_SUB_ACCOUNT);
//        }
//        if (StringUtils.isNotBlank(oldUserDataTemp.getUnionId())) {// 微信绑定
//            userStatus = BitUtils.enable(userStatus, Constant.USER_WEIXIN);
//        }

        user.setStatus(userStatus);
        return user;
    }

    /**
     * 转换UserInfo
     *
     * @param oldUserDataTemp
     * @param user
     * @param oldUser
     * @return
     */
    private UserInfo convertUserInfo(OldUserData oldUserDataTemp, User user, OldUser oldUser) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId()); // 用户Id
        // Long parent; // 主账户
        userInfo.setAgentId(StringUtils.isNotBlank(oldUser.getAgentId()) ? Long.valueOf(oldUser.getAgentId()) : null); // 推荐人
        userInfo.setAgentRewardRatio(oldUser.getAgentRewardRatio()); // 经纪人返佣比例
        userInfo.setTradingAccount(oldUser.getTradingAccount()); // 用户交易账户
        userInfo.setMakerCommission(oldUser.getMakerCommission()); // 被动方手续费
        userInfo.setTakerCommission(oldUser.getTakerCommission()); // 主动方手续费
        userInfo.setBuyerCommission(oldUser.getBuyerCommission()); // 买方交易手续费
        userInfo.setSellerCommission(oldUser.getSellerCommission()); // 卖方交易手续费
        userInfo.setDailyWithdrawCap(oldUserDataTemp.getWithdrawMaxAssetDay()); // 单日最大出金总金额
        userInfo.setDailyWithdrawCountLimit(oldUserDataTemp.getWithdrawMaxCountDay()); // 单日最大出金次数
        userInfo.setAutoWithdrawAuditThreshold(oldUserDataTemp.getReviewQuota()); // 免审核额度
        userInfo.setNickName(oldUser.getNickName()); // 昵称
        userInfo.setRemark(oldUser.getRemark()); // 备注
        userInfo.setTrackSource(oldUser.getTs()); // 注册渠道

        Date updateTime = oldUser.getUpdateTime();
        if (null == updateTime) {
            updateTime = DateUtils.getNewUTCDate();
        }
        userInfo.setUpdateTime(updateTime); // 更新时间
        userInfo.setInsertTime(oldUser.getCreateTime()); // 创建时间
        return userInfo;
    }

    /**
     * 转换UserIps
     *
     * @param oldUserIpKeyListTemp
     * @return
     */
    private List<UserIp> convertUserIps(List<OldUserIpKey> oldUserIpKeyListTemp) {
        List<UserIp> userIps = new ArrayList<>();
        if (oldUserIpKeyListTemp != null && oldUserIpKeyListTemp.size() > 0) {
            oldUserIpKeyListTemp.forEach(e -> {
                UserIp temp = new UserIp();
                temp.setUserId(Long.valueOf(e.getUserid()));
                temp.setIp(e.getIp());
                userIps.add(temp);
            });
        }
        return userIps;
    }

    /**
     * 转换UserSecurity
     *
     * @param oldUserDataTemp
     * @param oldUserSecurityTemp
     * @param user
     * @param oldUser
     * @return
     * @throws Exception
     */
    private UserSecurity convertUserSecurity(OldUserData oldUserDataTemp, OldUserSecurity oldUserSecurityTemp,
            User user, OldUser oldUser) throws Exception {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId()); // 用户id
        userSecurity.setEmail(user.getEmail()); // 用户邮箱
        userSecurity.setAntiPhishingCode(oldUserDataTemp.getAntiPhishingCode()); // 防钓鱼码
        userSecurity.setSecurityLevel(oldUserDataTemp.getSecurityLevel()); // 用户安全级别:1:普通,2:身份认证,3:?
        userSecurity.setMobileCode(oldUserDataTemp.getMobileCode()); // 手机国家编码
        userSecurity.setMobile(oldUser.getMobileNo()); // 手机
        userSecurity.setLoginFailedNum(null); // 当日密码错误次数 不需要同步
        userSecurity.setLoginFailedTime(null); // 登录失败日期 不需要同步
        // final String securityCipher = RedisCacheUtils.get(CacheKeys.SECURITY_CIPHER,
        // "Q8oYo6tna4LWlIhQjPX6XNMVtwqZOXJY",true);
        // final String authKey = EncryptionUtils.encryptAESToString(oldUserSecurityTemp.getSecretKey(),
        // securityCipher);// 谷歌2次验证
        final String authKey = StringUtils.trimToEmpty(oldUserSecurityTemp.getEncryptedSecretKey());// 谷歌2次验证
        userSecurity.setAuthKey(authKey); // 谷歌2次验证
        userSecurity.setDisableTime(oldUserSecurityTemp.getDisableTime()); // 禁用时间
        userSecurity.setUnbindTime(oldUserSecurityTemp.getUnbindTime()); // 解绑时间
        userSecurity.setLastLoginTime(oldUserSecurityTemp.getLastLoginTime()); // 最后登录时间
        Date lockEndTime = null;// 不更新锁定时间
        userSecurity.setLockEndTime(lockEndTime); // 锁定结束时间
        userSecurity.setInsertTime(oldUser.getCreateTime()); // 插入时间

        Date updateTime = oldUser.getUpdateTime();
        if (null == updateTime) {
            updateTime = DateUtils.getNewUTCDate();
        }
        userSecurity.setUpdateTime(updateTime); // 更新时间

        if (oldUserDataTemp.getSecurityLevel() != null) {
            userSecurity.setSecurityLevel(oldUserDataTemp.getSecurityLevel());
        }
        return userSecurity;
    }

    /**
     * 转换UserSecurityLogs
     *
     * @param oldUserLogListTemp
     * @return
     */
    private List<UserSecurityLog> convertUserSecurityLogs(List<OldUserLog> oldUserLogListTemp) {
        List<UserSecurityLog> userSecurityLogs = new ArrayList<>();
        if (oldUserLogListTemp != null && oldUserLogListTemp.size() > 0) {
            oldUserLogListTemp.forEach(e -> {
                UserSecurityLog temp = new UserSecurityLog();
                temp.setId(e.getId().longValue()); //
                temp.setUserId(Long.valueOf(e.getUserId())); // 用户id
                temp.setIp(e.getIpAddress()); // 用户ip
                temp.setIpLocation(e.getIpLocation()); // 用户ip所在位置
                temp.setClientType(e.getClientType()); // 客户端类型 ios android web wap
                temp.setOperateType(Constant.SECURITY_OPERATE_TYPE_LOGIN); // 操作类型
                temp.setOperateTime(e.getLoginTime()); // 操作时间
                temp.setDescription(e.getResInfo());; // 操作描述
                userSecurityLogs.add(temp);
            });
        }
        return userSecurityLogs;
    }

    /**
     * 转换UserCertificate
     *
     * @param oldUserIdPhotoTemp
     * @param user
     * @param userSecurity
     * @return
     */
    private UserCertificate convertUserCertificate(OldUserIdPhoto oldUserIdPhotoTemp, User user,
            UserSecurity userSecurity) {
        UserCertificate userCertificate = null;
        if (oldUserIdPhotoTemp != null) {
            userCertificate = new UserCertificate();
            userCertificate.setUserId(Long.valueOf(oldUserIdPhotoTemp.getUserid())); // 用户id
            userCertificate.setFront(oldUserIdPhotoTemp.getFront()); // 证件正面
            userCertificate.setBack(oldUserIdPhotoTemp.getBack()); // 证件反面
            userCertificate.setHand(oldUserIdPhotoTemp.getHand()); // 手持证件
            userCertificate.setFirstName(oldUserIdPhotoTemp.getFirstname()); //
            userCertificate.setLastName(oldUserIdPhotoTemp.getLastname()); //
            userCertificate.setMessage(oldUserIdPhotoTemp.getMessage()); // 消息
            userCertificate.setLastAuditor(oldUserIdPhotoTemp.getAuditor()); // 最后审核人
            userCertificate.setStatus(oldUserIdPhotoTemp.getStatus()); // 0,"未审核" , 1,"已审核" 2,"拒绝"
            userCertificate.setNumber(oldUserIdPhotoTemp.getNumber()); // 证件号码
            userCertificate.setType(oldUserIdPhotoTemp.getType()); // 类型:
            userCertificate.setSex(oldUserIdPhotoTemp.getSex()); // 性别:
            userCertificate.setCountry(oldUserIdPhotoTemp.getCountry()); // 国家
            userCertificate.setVersion(oldUserIdPhotoTemp.getVersion()); // 版本号
            Date updateTime = oldUserIdPhotoTemp.getUpdatetime();
            if (null == updateTime) {
                updateTime = DateUtils.getNewUTCDate();
            }
            userCertificate.setUpdateTime(updateTime); // 更新时间
            userCertificate.setInsertTime(oldUserIdPhotoTemp.getCreatetime()); // 创建时间
            if (userCertificate.getStatus().byteValue() == 1) {
                user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_CERTIFICATION));
                if (userSecurity.getSecurityLevel() == null) {
                    userSecurity.setSecurityLevel(2);
                } else {
                    if (userSecurity.getSecurityLevel().intValue() < 2) {
                        userSecurity.setSecurityLevel(2);
                    }
                }
            }
        }
        return userCertificate;
    }

    /**
     * 转换CompanyAuthentication
     *
     * @param oldCompanyAuthenticationTemp
     * @param user
     * @param userSecurity
     * @return
     */
    /*private CompanyCertificate convertCompanyCertificate(OldCompanyAuthentication oldCompanyAuthenticationTemp,
            User user, UserSecurity userSecurity) {
        CompanyCertificate companyCertificate = null;
        if (oldCompanyAuthenticationTemp != null) {
            companyCertificate = new CompanyCertificate();
            companyCertificate.setUserId(Long.valueOf(oldCompanyAuthenticationTemp.getUserId()));
            companyCertificate.setCompanyName(oldCompanyAuthenticationTemp.getCompanyName());
            companyCertificate.setCompanyAddress(oldCompanyAuthenticationTemp.getCompanyAddress());
            companyCertificate.setCompanyCountry(oldCompanyAuthenticationTemp.getCompanyCountry());
            companyCertificate.setApplyerName(oldCompanyAuthenticationTemp.getApplyerName());
            companyCertificate.setApplyerEmail(oldCompanyAuthenticationTemp.getApplyerEmail());
            companyCertificate.setStatus(oldCompanyAuthenticationTemp.getStatus());
            companyCertificate.setInfo(oldCompanyAuthenticationTemp.getInfo());
            companyCertificate.setInsertTime(oldCompanyAuthenticationTemp.getInsertTime());
            Date updateTime = oldCompanyAuthenticationTemp.getUpdateTime();
            if (null == updateTime) {
                updateTime = DateUtils.getNewUTCDate();
            }
            companyCertificate.setUpdateTime(updateTime);
            if (companyCertificate.getStatus().intValue() == 1) {
                user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_CERTIFICATION));
                user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_CERTIFICATION_TYPE));
                if (userSecurity.getSecurityLevel() == null) {
                    userSecurity.setSecurityLevel(2);
                } else {
                    if (userSecurity.getSecurityLevel().intValue() < 2) {
                        userSecurity.setSecurityLevel(2);
                    }
                }
            }
        }
        return companyCertificate;
    }*/
}
