package com.binance.account.data.mapper.device;

import com.binance.account.data.entity.device.UserDeviceProperty;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.List;

@DefaultDB
@Monitored
public interface UserDevicePropertyMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(UserDeviceProperty record);

    UserDeviceProperty selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDeviceProperty record);

    List<UserDeviceProperty> selectByTypeAndStatus(@Param("agentType") String agentType, @Param("status") Byte status);
}