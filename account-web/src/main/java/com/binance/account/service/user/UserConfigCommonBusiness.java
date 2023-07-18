package com.binance.account.service.user;

import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.service.userconfig.IUserConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Log4j2
@Service
public class UserConfigCommonBusiness {
    @Resource
    private IUserConfig iUserConfig;

    @Autowired
    private UserInfoMapper userInfoMapper;



    public Integer addOrUpdateUserConfig(Long userId, String configType, String configName) throws Exception {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType(configType);
        uc.setConfigName(configName);
        try {
            String keyCode = String.valueOf(userId) + "_" + configType;
            // 没有默认配置项，则添加默认配置项 清除缓存
            iUserConfig.insertOrupdateUserConfig(keyCode, uc);
            return 1;
        } catch (Exception e) {
            log.error("addOrUpdateUserConfig,configType:{}, userId:{}", configType, userId);
            log.error("addOrUpdateUserConfig error-->{}", e);
            return 0;
        }
    }


    public String getConfigByConfigType(Long userId,String configType)  {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType(configType);
        UserConfig result=userInfoMapper.selectLatestUserConfig(uc);
        if(null==result){
            return null;
        }
        return result.getConfigName();
    }


}
