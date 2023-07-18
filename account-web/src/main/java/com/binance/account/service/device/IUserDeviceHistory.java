package com.binance.account.service.device;

import java.util.List;

import com.binance.account.data.entity.device.UserDeviceHistory;

public interface IUserDeviceHistory {

    void addHistory(UserDeviceHistory history);

    List<UserDeviceHistory> listHistory(Long userId, Long deviceId);
}
