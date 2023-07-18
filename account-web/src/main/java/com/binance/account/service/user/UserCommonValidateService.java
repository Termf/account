package com.binance.account.service.user;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.enums.AccountTypeEnum;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author pengchenxue
 * 这个类的作用很明显
 * 大量验证代码都随机的散播在各种类的方法里面，不统一
 * 我这边想把一些验证方法都抽到一个service里面这个更清晰
 * 验证的类，我的设想是按照业务划分，不同的业务可以应该开一个新的类
 * 当然如果你是有关联关系的业务，比方说子账号和普通账号的验证，那么可以继承
 */
@Log4j2
@Service
public class UserCommonValidateService {
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    private IUserKyc kyc;

    @Value("${marginCountryBlackList:}")
    private String marginCountryBlackList;

    @Value("${futureIpCountryBlackList:}")
    private String futureIpCountryBlackList;
    /**
     * 判断这种userid是否是margin账户
     * */
    public Boolean isMarginUser(Long userId) {
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
        //检查传进来的userid是否是margin的userid
        //当前账号不能是margin 账号
        Boolean isMarginUser = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_MARGIN_USER);
        log.info("UserCommonValidateService.isMarginUser: userId={},isMarginUser={}", userId, isMarginUser);
        return isMarginUser;
    }


    /**
     * 判断这种userid是否是margin账户
     * */
    public Boolean isMarginUserOrIsolatedMargin(Long userId) {
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
        //检查传进来的userid是否是margin的userid
        //当前账号不能是margin 账号
        Boolean isMarginUser = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_MARGIN_USER);
        Boolean isIsolatedMarginUser = BitUtils.isEnable(rootUser.getStatus(), AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER);
        log.info("UserCommonValidateService.isMarginUserOrIsolatedMargin: userId={},isMarginUser={},isIsolatedMarginUser={}", userId, isMarginUser,isIsolatedMarginUser);
        return isMarginUser|| isIsolatedMarginUser;
    }


    /**
     * 判断这种userid是否是future账户
     * */
    public Boolean isFutureUser(Long userId) {
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
        Boolean isFutureUser = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_FUTURE_USER);
        log.info("UserCommonValidateService.isFutureUser: userId={},isFutureUser={}", userId, isFutureUser);
        return isFutureUser;
    }


    /**
     * 判断这种userid是否是fiat账户
     * */
    public Boolean isFiatUser(Long userId) {
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
        //检查传进来的userid是否是fiat的userid
        //当前账号不能是fiat 账号
        Boolean isFiatUser = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_FIAT_USER);
        log.info("UserCommonValidateService.isFiatUser: userId={},isFiatUser={}", userId, isFiatUser);
        return isFiatUser;
    }

    /**
     * 判断这种agentId是否合法
     * */
    public Boolean isValidateAgentId(Long agentId) {
        if (null == agentId) {
            return false;
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(agentId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            return false;
        }
        final User agentUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == agentUser) {
            return false;
        }
        Boolean isValidateAgentId =false;
        //首先不能是margin用户
        Integer accountType = AccountTypeEnum.getAccountType(agentUser.getStatus());
        if (accountType == null || accountType.equals(AccountTypeEnum.MARGIN.getAccountType()) || accountType.equals(AccountTypeEnum.FUTURE.getAccountType()) || accountType.equals(AccountTypeEnum.FIAT.getAccountType())
                || !BitUtils.isEnable(agentUser.getStatus(), Constant.USER_ACTIVE)){
            return false;
        }
        //id要大于零并且不是null
        Boolean agentIdValidate=agentId !=null && agentId > 0L;
        //id要存在
        UserIndex tempUserIndex = this.userIndexMapper.selectByPrimaryKey(agentId);
        Boolean userIsNotExist=null==tempUserIndex;
        if(!agentIdValidate||userIsNotExist){
            isValidateAgentId =false;
        }else {
            isValidateAgentId =true;
        }
        log.info("UserCommonValidateService.isValidateAgentId: agentId={},isValidateAgentId={}", agentId, isValidateAgentId);
        return isValidateAgentId;
    }


    /**
     * 判断这种userid是否是子账户
     * */
    public Boolean isSubUser(Long userId) {
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
        //检查传进来的userid是否是子账号的userid
        //当前账号不能是子账号
        Boolean isSubUser = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        log.info("UserCommonValidateService.isSubUser: userId={},isSubUser={}", userId, isSubUser);
        return isSubUser;
    }


    /**
     * 判断当前用户的ip是否在国家黑名单
     */
    public void checkCountryBackListByIp(Long userId) throws Exception {
        final String ip = WebUtils.getRequestIp();
        log.info("checkCountryBackListByIp userId={},ip={}", userId, ip);
        if (org.apache.commons.lang3.StringUtils.isBlank(futureIpCountryBlackList)) {
            return;
        }
        String[] countryBlackArry = futureIpCountryBlackList.split(",");
        List<String> countryBlackList = Lists.newArrayList(countryBlackArry);
        String countryCode = null;
        try {
            countryCode = IP2LocationUtils.getCountryShort(ip);
        } catch (Exception e) {
            log.error("checkCountryBackListByIp.getCountryShort error:", e);
            return;
        }
        if (countryBlackList.contains(countryCode)) {
            throw new BusinessException(AccountErrorCode.MARGIN_IP_COUNTRY_NOT_SPPORT);
        }
    }

    /**
     * 判断当前用户的国家是否在国家黑名单
     */
    public void checkKycCountryBackList(Long userId) throws Exception {
        UserKycCountryResponse userKycCountryResponse= kyc.getKycCountry(userId);
        String countryCode=null;
        if (null != userKycCountryResponse
                && org.apache.commons.lang3.StringUtils.isNotBlank(userKycCountryResponse.getCountryCode())) {
            countryCode=userKycCountryResponse.getCountryCode();
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(countryCode)) {
            checkCountryBackListByIp(userId);
            return;
        }
        if(org.apache.commons.lang3.StringUtils.isAnyBlank(countryCode,marginCountryBlackList)){
            return;
        }
        String[] countryBlackArry = marginCountryBlackList.split(",");
        List<String> countryBlackList=Lists.newArrayList(countryBlackArry);
        if(countryBlackList.contains(countryCode)){
            throw new BusinessException(AccountErrorCode.COUNTRY_KYC_NOT_SPPORT);
        }
        return ;
    }
}
