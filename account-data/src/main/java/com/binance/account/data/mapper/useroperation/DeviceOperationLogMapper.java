package com.binance.account.data.mapper.useroperation;

import com.binance.account.data.entity.log.DeviceOperationLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.Collection;
import java.util.Date;
import java.util.List;



@DefaultDB
@Monitored
public interface DeviceOperationLogMapper {

    int insert(DeviceOperationLog record);


    List<DeviceOperationLog> queryDeviceOperationLogPage(@Param("devicePks") Collection<Long> devicePks, @Param("userId") Long userId,
                                                         @Param("operation") String operation, @Param("userOperationLogUuid") String userOperationLogUuid,
                                                         @Param("timeFrom") Date timeFrom, @Param("timeTo") Date timeTo,
                                                         @Param("limit") int limit, @Param("offset") int offset);

    Long queryDeviceOperationLogPageCount(@Param("devicePks") Collection<Long> devicePks, @Param("userId") Long userId,
                                          @Param("operation") String operation, @Param("userOperationLogUuid") String userOperationLogUuid,
                                          @Param("timeFrom") Date timeFrom, @Param("timeTo") Date timeTo);

}

