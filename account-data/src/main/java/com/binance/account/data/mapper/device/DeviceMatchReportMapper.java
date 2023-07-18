package com.binance.account.data.mapper.device;

import com.binance.account.data.entity.device.DeviceMatchReport;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.Date;

@DefaultDB
@Monitored
public interface DeviceMatchReportMapper {

    Integer insert(DeviceMatchReport record);

    Integer batchDeleteBefore(@Param("id") Long id);

    Long selectLastIdBefore(@Param("insertTime") Date insertTime);

}