package com.binance.account.service.datamigration.impl;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.UserCertificate;
import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.certificate.UserCertificateMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Created by Fei.Huang on 2018/4/9.
 */
@Log4j2
public class BaseDataMigrationBusiness {

    @Resource
    protected UserMapper userMapper;
    @Resource
    private UserIpMapper userIpMapper;
    @Resource
    protected UserInfoMapper userInfoMapper;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private UserMobileIndexMapper userMobileIndexMapper;
    @Resource
    private UserCertificateMapper userCertificateMapper;
    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    @Resource
    private UserTradingAccountMapper userTradingAccountMapper;
    @Resource
    private UserCertificateIndexMapper userCertificateIndexMapper;
    @Resource
    protected com.binance.account.data.mapper.user.UserIndexMapper userIndexMapper;
    @Resource
    protected IMsgNotification iMsgNotification;

    /**
     * 同步UserIndex
     *
     * @param user
     */
    protected void syncUserIndex(User user) {
        int status = this.userIndexMapper.insertIgnore(new UserIndex(user.getUserId(), user.getEmail()));
        log.info("syncUserIndex status:{}", status);
        if (status <= 0) {
            // 证明修改过账号 更新邮箱索引删除老的用户数据
            log.info("syncUserIndex userId:{}, email:{}", user.getUserId(), user.getEmail());
            final UserIndex userIndexTemp = this.userIndexMapper.selectByPrimaryKey(user.getUserId());

            Assert.notNull(userIndexTemp, "userIndexTemp isNull");

            log.info("syncUserIndex userIndexTemp isNull:{}, userIndexTemp.userId:{}, userIndexTemp.email:{}",
                    Objects.isNull(userIndexTemp),
                    Objects.isNull(userIndexTemp) ? StringUtils.EMPTY : userIndexTemp.getUserId(),
                    Objects.isNull(userIndexTemp) ? StringUtils.EMPTY : userIndexTemp.getEmail());

            this.userIndexMapper.updateByPrimaryKeySelective(new UserIndex(user.getUserId(), user.getEmail()));

            if (null != userIndexTemp && !StringUtils.equals(userIndexTemp.getEmail(), user.getEmail())) {
                // 账号修改过执行
                this.userMapper.deleteByEmail(userIndexTemp.getEmail());
            }


        }
        log.info("synchron UserIndex done, userId:{}", user.getUserId());
    }

    /**
     * 同步User
     *
     * @param user
     */
    protected void syncUser(User user) {
        int status = this.userMapper.insertIgnore(user);
        if (status <= 0) {
            this.userMapper.updateByEmail(user);
        }
        log.info("synchron User done, userId:{}", user.getUserId());
    }

    /**
     * 同步UserInfo
     *
     * @param user
     * @param userInfo
     */
    protected void syncUserInfo(User user, UserInfo userInfo) {
        if (null != userInfo) {
            if (null == userInfo.getUpdateTime()) {
                userInfo.setUpdateTime(DateUtils.getNewUTCDate());
            }
            if (null != userInfo.getTradingAccount()) {
                UserTradingAccount userTradingAccount = new UserTradingAccount();
                userTradingAccount.setTradingAccount(userInfo.getTradingAccount());
                userTradingAccount.setUserId(userInfo.getUserId());
                // 交易账号肯定是不会变动的
                this.userTradingAccountMapper.insertIgnore(userTradingAccount);
            }
            int status = this.userInfoMapper.insertIgnore(userInfo);
            if (status <= 0) {
                this.userInfoMapper.updateByPrimaryKey(userInfo);
            }
        }
        log.info("synchron UserInfo done, userId:{}", user.getUserId());
    }

    /**
     * 同步UserIps
     *
     * @param user
     * @param userIps
     */
    protected void syncUserIps(User user, List<UserIp> userIps) {
        if (!CollectionUtils.isEmpty(userIps)) {
            for (UserIp userIp : userIps) {
                this.userIpMapper.insertIgnore(userIp);
            }
        }
        log.info("synchron UserIps done, userId:{}", user.getUserId());
    }

    /**
     * 同步UserSecurity
     *
     * @param user
     * @param userSecurity
     */
    protected void syncUserSecurity(User user, UserSecurity userSecurity) {
        int status = this.userSecurityMapper.insertIgnore(userSecurity);
        log.info("syncUserSecurity, userId:{}, status:{}", user.getUserId(), status);
        UserSecurity userSecurityTemp = null;
        if (status <= 0) {
            userSecurityTemp = this.userSecurityMapper.selectByPrimaryKey(userSecurity.getUserId());
            this.userSecurityMapper.updateByPrimaryKey(userSecurity);
        }
        // 有可能解绑了手机
        if (null != userSecurityTemp && StringUtils.isNotBlank(userSecurityTemp.getMobile())) {
            // 先删除之前的手机号
            this.userMobileIndexMapper.deleteByPrimaryKey(userSecurityTemp.getMobile(),
                    userSecurityTemp.getMobileCode());
        }
        if (null != userSecurity.getMobile()) {
            // 插入手机索引
            UserMobileIndex userMobileIndex = new UserMobileIndex();
            userMobileIndex.setUserId(user.getUserId());
            userMobileIndex.setMobile(userSecurity.getMobile());
            userMobileIndex.setCountry(userSecurity.getMobileCode());
            status = this.userMobileIndexMapper.insertIgnore(userMobileIndex);
            log.info("syncUserSecurity usermobileIndexMapper, userId:{}, status:{}", user.getUserId(), status);
            if (status <= 0) {
                this.userMobileIndexMapper.updateSelective(userMobileIndex);
            }
        }
        log.info("synchron UserSecurity done, userId:{}", user.getUserId());
    }

    /**
     * 同步UserSecurityLogs
     *
     * @param user
     * @param userSecurityLogs
     */
    protected void syncUserSecurityLogs(User user, List<UserSecurityLog> userSecurityLogs) {
        if (!CollectionUtils.isEmpty(userSecurityLogs)) {
            for (UserSecurityLog userSecurityLog : userSecurityLogs) {
                this.userSecurityLogMapper.insertIgnoreId(userSecurityLog);
            }
        }
        log.info("synchron UserSecurityLogs done, userId:{}", user.getUserId());
    }

//    /**
//     * 同步UserCertificate
//     *
//     * @param user
//     * @param userCertificate
//     */
//    protected void syncUserCertificate(User user, UserCertificate userCertificate) {
//        if (null != userCertificate) {
//            if (userCertificate.getUpdateTime() == null) {
//                userCertificate.setUpdateTime(DateUtils.getNewUTCDate());
//            }
//            UserCertificate userCertificateTemp =
//                    this.userCertificateMapper.selectByPrimaryKey(userCertificate.getUserId());
//            int status = this.userCertificateMapper.insertIgnore(userCertificate);
//            if (status <= 0) {
//                this.userCertificateMapper.updateByPrimaryKey(userCertificate);
//            }
//            // 审核通过添加证件索引
//            if (userCertificate.getStatus().byteValue() == (byte) 1) {
//                UserCertificateIndex userCertificateIndex = new UserCertificateIndex();
//                userCertificateIndex.setCountry(userCertificate.getCountry());
//                userCertificateIndex.setNumber(userCertificate.getNumber());
//                userCertificateIndex.setUserId(userCertificate.getUserId());
//                this.userCertificateIndexMapper.insertIgnore(userCertificateIndex);
//            }
//        }
//        log.info("synchron UserCertificate done, userId:{}", user.getUserId());
//    }

    /**
     * 同步CompanyCertificate
     *
     * @param user
     * @param companyCertificate
     */
    /*protected void syncCompanyCertificate(User user, CompanyCertificate companyCertificate) {
        if (null != companyCertificate) {
            if (companyCertificate.getUpdateTime() == null) {
                companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
            }
            int status = this.companyCertificateMapper.insertIgnore(companyCertificate);
            if (status <= 0) {
                this.companyCertificateMapper.updateByPrimaryKey(companyCertificate);
            }
        }
        log.info("synchron CompanyCertificate done, userId:{}", user.getUserId());
    }*/
}
