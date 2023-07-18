package com.binance.account.service.device.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.device.UserDeviceHistory;
import com.binance.account.data.mapper.device.UserDeviceHistoryMapper;
import com.binance.account.service.device.IUserDeviceHistory;

@Service
public class UserDeviceHistoryBusiness implements IUserDeviceHistory {

    @Autowired
    private UserDeviceHistoryMapper userDeviceHistoryMapper;

    @Override
    public void addHistory(UserDeviceHistory history) {
        userDeviceHistoryMapper.insertSelective(history);
    }

    @Override
    public List<UserDeviceHistory> listHistory(Long userId, Long deviceId) {
        return userDeviceHistoryMapper.selectByUserIdAndDeviceId(userId, deviceId);
    }
}
