package com.binance.account.service.device.impl;

import com.binance.account.data.entity.device.DeviceMatchReport;
import com.binance.account.data.mapper.device.DeviceMatchReportMapper;
import com.binance.account.service.device.IDeviceMatchReport;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Log4j2
@Service
public class DeviceMatchReportBusiness implements IDeviceMatchReport {

    @Resource
    private DeviceMatchReportMapper deviceMatchReportMapper;

    @Override
    @Async
    public Integer insertDeviceMatchReport(DeviceMatchReport report) {
        return deviceMatchReportMapper.insert(report);
    }

}
