package com.binance.account.data.mapper.device;

import com.binance.account.data.entity.device.UserDeviceHistory;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.List;

@DefaultDB
@Monitored
public interface UserDeviceHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDeviceHistory record);

    int insertSelective(UserDeviceHistory record);

    UserDeviceHistory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDeviceHistory record);

    int updateByPrimaryKey(UserDeviceHistory record);

    List<UserDeviceHistory> selectByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") Long deviceId);
}