package com.binance.account.service.operationlog.impl;

import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.log.DeviceOperationLog;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.useroperation.DeviceOperationLogMapper;
import com.binance.account.service.device.impl.UserDeviceBusiness;
import com.binance.account.service.operationlog.IDeviceOperationLog;
import com.binance.account.vo.operationlog.DeviceOperationLogVo;
import com.binance.account.vo.security.request.DeviceOperationLogRequest;
import com.binance.account.vo.security.response.DeviceOperationLogResultResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DeviceOperationLogBusiness implements IDeviceOperationLog {


    @Resource
    private DeviceOperationLogMapper deviceOperationLogMapper;

    @Resource
    private UserDeviceBusiness userDeviceBusiness;

    @Resource
    private UserIndexMapper userIndexMapper;

    @Override
    public DeviceOperationLogResultResponse queryDeviceOperationLogPage(DeviceOperationLogRequest request) throws Exception {
        if (request.getUserId() == null && request.getDevicePk() == null) {
            throw new IllegalArgumentException("Either userId or devicePk should have value.");
        }
        if (request.getLimit() <= 0 || request.getLimit() > 100000) {
            request.setLimit(20);
        }

        DeviceOperationLogResultResponse deviceOperationLogResultResponse = new DeviceOperationLogResultResponse();
        List<Long> devicePks = null;
        if (request.getDevicePk() != null) {
            devicePks = Collections.singletonList(request.getDevicePk());
        } else {
            List<UserDevice> userDevices = userDeviceBusiness.listDevice(
                    request.getUserId(), null, null, null,
                    null, true, null, null);
            if (CollectionUtils.isNotEmpty(userDevices)) {
                devicePks = userDevices.stream().map(ud -> ud.getId()).collect(Collectors.toList());
            }
        }
        Long count = deviceOperationLogMapper.queryDeviceOperationLogPageCount(
                devicePks, request.getUserId(), request.getOperation(), null,
                request.getTimeFrom(), request.getTimeTo());

        deviceOperationLogResultResponse.setTotal(count);
        if (count == 0) {
            return deviceOperationLogResultResponse;
        }

        List<DeviceOperationLog> deviceOperationLogs = deviceOperationLogMapper.queryDeviceOperationLogPage(
                devicePks, request.getUserId(), request.getOperation(), null,
                request.getTimeFrom(), request.getTimeTo(),
                request.getLimit(), request.getOffset());

        List<DeviceOperationLogVo> result = new ArrayList<>(deviceOperationLogs.size());
        if (CollectionUtils.isNotEmpty(deviceOperationLogs)) {
            UserIndex userIndex = userIndexMapper.selectByPrimaryKey(deviceOperationLogs.get(0).getUserId());
            String email = null;
            if (userIndex != null) {
                email = userIndex.getEmail();
            }
            for (DeviceOperationLog log : deviceOperationLogs) {
                DeviceOperationLogVo logVo = new DeviceOperationLogVo();
                BeanUtils.copyProperties(log, logVo);
                logVo.setEmail(email);
                result.add(logVo);
            }
        }
        deviceOperationLogResultResponse.setRows(result);
        return deviceOperationLogResultResponse;
    }
}
