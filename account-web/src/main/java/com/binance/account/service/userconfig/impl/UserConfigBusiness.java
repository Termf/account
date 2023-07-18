package com.binance.account.service.userconfig.impl;

import com.binance.account.data.entity.log.UserPreferLog;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.useroperation.UserPreferLogMapper;
import com.binance.account.service.userconfig.IUserConfig;
import com.binance.master.constant.CacheKeys;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.binance.account.constants.AccountConstants.USER_CONFIG_NATIVE_CURRENCY;
import static com.binance.account.constants.AccountConstants.USER_CONFIG_PREFER_LANG;

/**
 * Created by mengjuan on 2018/11/29.
 */
@Log4j2
@Service
public class UserConfigBusiness implements IUserConfig {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserPreferLogMapper userPreferLogMapper;

    @Cacheable(value = CacheKeys.USER_CONFIG_CODE, key = "#keyCode")
    @Override
    public List<UserConfig> selectUserConfigList(String keyCode, Map<String, Object> params) {
        return userInfoMapper.selectUserConfigList(params);
    }

    @CacheEvict(value = CacheKeys.USER_CONFIG_CODE, key = "#keyCode")
    @Override
    public void updateUserConfig(String keyCode, UserConfig uc) {
        userInfoMapper.updateUserConfig(uc);
        log.info("清除缓存：{}, key:{}", CacheKeys.USER_CONFIG_CODE, keyCode);
    }

    @CacheEvict(value = CacheKeys.USER_CONFIG_CODE, key = "#keyCode")
    @Override
    public void insertUserConfig(String keyCode, UserConfig uc) {
        userInfoMapper.insertUserConfig(uc);
        log.info("清除缓存：{}, key:{}", CacheKeys.USER_CONFIG_CODE, keyCode);
    }

    @CacheEvict(value = CacheKeys.USER_CONFIG_CODE, key = "#keyCode")
    @Override
    public void insertOrupdateUserConfig(String keyCode, UserConfig uc) {
        userInfoMapper.insertOrUpdateUserConfig(uc);

        // 偏好语言、汇率变更记录日志
        try {
            if (StringUtils.equalsAny(uc.getConfigType(), USER_CONFIG_PREFER_LANG, USER_CONFIG_NATIVE_CURRENCY)) {
                UserPreferLog preferLog = new UserPreferLog();
                preferLog.setUserId(uc.getUserId());
                preferLog.setPreferType(uc.getConfigType());
                preferLog.setPreferVal(uc.getConfigName());
                userPreferLogMapper.insert(preferLog);
            }
        } catch (Exception e) {
            log.error("UserConfigBusiness.insertOrupdateUserConfig,record prefer log error", e);
        }

        log.info("清除缓存：{}, key:{}", CacheKeys.USER_CONFIG_CODE, keyCode);
    }
}
