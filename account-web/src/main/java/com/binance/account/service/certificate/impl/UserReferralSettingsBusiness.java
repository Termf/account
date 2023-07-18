package com.binance.account.service.certificate.impl;

import com.binance.account.data.entity.user.UserReferralSettings;
import com.binance.account.data.mapper.user.UserReferralSettingsMapper;
import com.binance.account.service.certificate.IUserReferralSettings;
import com.binance.account.vo.user.request.UserReferralSettingsRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Log4j2
@Service
public class UserReferralSettingsBusiness implements IUserReferralSettings {
    @Resource
    private UserReferralSettingsMapper userReferralSettingsMapper;

    @Override
    public APIResponse<?> submit(@RequestBody() APIRequest<UserReferralSettingsRequest> request) {
        UserReferralSettingsRequest requestBody = request.getBody();
        UserReferralSettings userReferralSettings = new UserReferralSettings();
        BeanUtils.copyProperties(requestBody, userReferralSettings);
        Long userId = requestBody.getUserId();
        try {
            UserReferralSettings existingUserReferralSettings = userReferralSettingsMapper.queryByUserId(userId);
            //已经提交过，无需更改。此数据仅作备用，没有实际的作用
            if (Objects.isNull(existingUserReferralSettings)) {
                userReferralSettings.setCreateTime(new Date());
                userReferralSettingsMapper.insert(userReferralSettings);
            }
            else {
                log.warn("User {} has already submitted userReferralSettings,time:{}.", userId, DateUtils.formatter(existingUserReferralSettings.getCreateTime(),DateUtils.DETAILED_NUMBER_PATTERN));
            }
        }
        catch (Exception e) {
            log.error("failed to insert userReferralSettings.user:"+ userId, e);
            return APIResponse.getErrorJsonResult("提交用户返佣设置失败.");
        }

        return APIResponse.getOKJsonResult("提交用户返佣设置成功.");
    }
    
}
