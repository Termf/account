package com.binance.account.mq;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.log.DeviceOperationLog;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.mapper.useroperation.DeviceOperationLogMapper;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.UserOperationLogDto;
import com.binance.account.service.device.impl.UserDeviceBusiness;
import com.binance.account.service.security.IUserIpChange;
import com.binance.account.utils.StringUtils;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Log4j2
@Component
public class UserOperationMsgListener {

    @Autowired
    private UserOperationLogMapper userOperationLogMapper;

    @Autowired
    private UserDeviceBusiness userDeviceBusiness;

    @Autowired
    private IUserIpChange userIpChange;

    @Autowired
    private DeviceOperationLogMapper deviceOperationLogMapper;

    private static final String USER_OPERATION_LOG_QUEUE = "user_operation_log";


    @RabbitListener(queues = USER_OPERATION_LOG_QUEUE)
    public void onMessage(Message message, Channel channel) {
        String body = null;
        final long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            body = new String(message.getBody(), "UTF-8");
            log.info("user operation log msg");
            if (org.apache.commons.lang3.StringUtils.isBlank(body)) {
                log.warn("user operation log msg body is blank");
                return;
            }
            save(body);
//            try {
//                channel.basicAck(deliveryTag, false);
//            } catch (Exception e) {
//                log.error("消息ACK失败. ", e);
//            }
        } catch (Exception e) {
            log.warn("UserOperationMsgListener error body: " + body, e);
//            try {
//                channel.basicNack(deliveryTag, false, true);
//            } catch (Exception e1) {
//                log.error("消息NACK失败.", e);
//            }

        } finally {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("消息ACK失败. ", e);
            }
        }
    }

    private UserOperationLog fitLog(UserOperationLogDto userOperationLogDto) {
        UserOperationLog userOperationLog = new UserOperationLog();
        BeanUtils.copyProperties(userOperationLogDto, userOperationLog);
        //所有外部传入的数据都要处理emoji字符。
        userOperationLog.setOperation(userOperationLog.getOperation());
        userOperationLog.setClientType(org.apache.commons.lang3.StringUtils.abbreviate(userOperationLog.getClientType(), 10));
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userOperationLog.getVersionCode())) {
            userOperationLog.setVersionCode(org.apache.commons.lang3.StringUtils.abbreviate(StringUtils.replaceEmoji(userOperationLog.getVersionCode(), ""), 10));
        }
        userOperationLog.setRealIp(org.apache.commons.lang3.StringUtils.abbreviate(userOperationLog.getRealIp(), 15));
        userOperationLog.setFullIp(org.apache.commons.lang3.StringUtils.abbreviate(userOperationLog.getFullIp(), 84));
        userOperationLog.setApikey(org.apache.commons.lang3.StringUtils.abbreviate(userOperationLog.getApikey(), 64));
        if (userOperationLog.getUserAgent() != null) {
            userOperationLog.setUserAgent(org.apache.commons.lang3.StringUtils.abbreviate(StringUtils.replaceEmoji(userOperationLog.getUserAgent(), ""), 332));
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userOperationLog.getRequest())) {
            userOperationLog.setRequest(org.apache.commons.lang3.StringUtils.abbreviate(StringUtils.replaceEmoji(userOperationLog.getRequest(), ""), 1000));
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userOperationLog.getResponse())) {
            userOperationLog.setResponse(org.apache.commons.lang3.StringUtils.abbreviate(StringUtils.replaceEmoji(userOperationLog.getResponse(), ""), 2000));
        }
        userOperationLog.setResponseStatus(org.apache.commons.lang3.StringUtils.abbreviate(userOperationLog.getResponseStatus(), 84));
        return userOperationLog;
    }

    private DeviceOperationLog buildDeviceOperationLog(UserOperationLogDto userOperationLogDto) {
        Map<String, String> candidate = userOperationLogDto.getDeviceInfo();
        List<UserDevice> deviceList = userDeviceBusiness.listDevice(userOperationLogDto.getUserId(),
                userOperationLogDto.getClientType(), null, null, null, false, null, null);
        boolean isHistoryIp = userIpChange.isHistoryIp(userOperationLogDto.getUserId(), candidate.get(UserDevice.LOGIN_IP));
        UserDeviceBusiness.DeviceDiffer deviceDiffer = userDeviceBusiness.findMostSimilarDevice(deviceList, candidate, userOperationLogDto.getClientType(), isHistoryIp);

        DeviceOperationLog deviceOperationLog = new DeviceOperationLog();
        deviceOperationLog.setUserId(userOperationLogDto.getUserId());
        deviceOperationLog.setDeviceInfo(JSON.toJSONString(userOperationLogDto.getDeviceInfo()));
        deviceOperationLog.setIp(userOperationLogDto.getRealIp());
        deviceOperationLog.setNote(userOperationLogDto.getDeviceNote());
        deviceOperationLog.setOperation(userOperationLogDto.getOperation());
        deviceOperationLog.setTime(userOperationLogDto.getRequestTime());
        deviceOperationLog.setUserOperationLogUuid(userOperationLogDto.getUuid());

        if (deviceDiffer != null && deviceDiffer.isSame()) {
            log.info("用户: {}, 操作: {}--{}, 与历史设备id: {} 匹配通过",
                    userOperationLogDto.getUserId(), userOperationLogDto.getOperation(), userOperationLogDto.getUuid(), deviceDiffer.getMatched().getId());

            UserDevice matched = deviceDiffer.getMatched();
            deviceOperationLog.setDevicePk(matched.getId());
            deviceOperationLog.setScore(String.format("%.2f", deviceDiffer.getScore()));
        } else {
            AddUserDeviceResponse addUserDeviceResponse = userDeviceBusiness.addDevice(userOperationLogDto.getUserId(), userOperationLogDto.getClientType(),
                    UserDevice.Status.NOT_AUTHORIZED, userOperationLogDto.getOperation(), userOperationLogDto.getDeviceInfo());
            if (addUserDeviceResponse != null) {
                deviceOperationLog.setDevicePk(addUserDeviceResponse.getId());
                deviceOperationLog.setScore("0");
                log.info("用户: {}, 操作: {}--{}, 与历史设备匹配不通过，新建设备id: {}",
                        userOperationLogDto.getUserId(), userOperationLogDto.getOperation(), userOperationLogDto.getUuid(), addUserDeviceResponse.getId());
            }
        }
        return deviceOperationLog;
    }

    private void save(String json) {
        UserOperationLogDto userOperationLogDto = JSON.parseObject(json, UserOperationLogDto.class);
        if (userOperationLogDto.getUserId() != null) {
            UserOperationLog userOperationLog = fitLog(userOperationLogDto);
            userOperationLogMapper.insert(userOperationLog);
        }

        if (userOperationLogDto.getUserId() != null &&
                BooleanUtils.toBoolean(userOperationLogDto.getLogDeviceInfo()) &&
                MapUtils.isNotEmpty(userOperationLogDto.getDeviceInfo())) {
            DeviceOperationLog deviceOperationLog = buildDeviceOperationLog(userOperationLogDto);
            if (deviceOperationLog != null && deviceOperationLog.getDevicePk() != null) {
                deviceOperationLogMapper.insert(deviceOperationLog);
            } else {
                log.info("deviceOperationLog pk is null. device info: {}", JSON.toJSONString(deviceOperationLog));
            }
        }
    }

}
