package com.binance.account.service.userconfig;

import com.binance.account.data.entity.user.UserConfig;
import java.util.List;
import java.util.Map;

/**
 * Created by mengjuan on 2018/11/29.
 */
public interface IUserConfig {
    public List<UserConfig> selectUserConfigList(String keyCode, Map<String, Object> params);

    public void updateUserConfig(String keyCode, UserConfig uc);

    public void insertUserConfig(String keyCode, UserConfig uc);

    public void insertOrupdateUserConfig(String keyCode, UserConfig uc);
}