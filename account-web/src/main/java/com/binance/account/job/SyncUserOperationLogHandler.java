package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.UserOperationForBigDataDto;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * @author szlong
 */
@Log4j2
@JobHandler(value = "syncUserOperationLogHandler")
@Component
public class SyncUserOperationLogHandler extends IJobHandler {

    private static final String TOPIC = "bnbUserOperationLog";

    private final UserOperationLogMapper userOperationLogMapper;
    private final KafkaTemplate kafkaTemplate;

    public SyncUserOperationLogHandler(UserOperationLogMapper userOperationLogMapper
            , @Autowired @Qualifier("bigDataKafkaTemplate") KafkaTemplate kafkaTemplate) {
        this.userOperationLogMapper = userOperationLogMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public ReturnT<String> execute(String s) {
        log.info("syncUserOperationLogHandler start [{}]", s);
        JSONObject paramObject = JSON.parseObject(s);
        Date start = paramObject.getDate("start");
        Date end = Objects.requireNonNull(paramObject.getDate("end"));
        int batchSize = paramObject.getIntValue("batchSize");
        long userId = paramObject.getLongValue("userId");
        int skip = 0;
        List<UserOperationLog> userOperationLogs;
        while (CollectionUtils.isNotEmpty(userOperationLogs = userOperationLogMapper.page(skip, batchSize, start, end, userId))) {
            log.info("userOperationLogs [{}]", userOperationLogs.size());
            userOperationLogs.forEach(userOperationLog -> {
                UserOperationForBigDataDto userOperationForBigDataDto = new UserOperationForBigDataDto();
                userOperationForBigDataDto.setUserId(userOperationLog.getUserId() + "");
                userOperationForBigDataDto.setEmail(userOperationLog.getEmail());
                userOperationForBigDataDto.setClientType(userOperationLog.getClientType());
                userOperationForBigDataDto.setVersionCode(userOperationLog.getVersionCode());
                userOperationForBigDataDto.setRealIp(userOperationLog.getRealIp());
                userOperationForBigDataDto.setFullIp(userOperationLog.getFullIp());
                userOperationForBigDataDto.setUserAgent(userOperationLog.getUserAgent());
                userOperationForBigDataDto.setRequestTime(userOperationLog.getRequestTime());
                userOperationForBigDataDto.setResponseTime(userOperationLog.getResponseTime());
                userOperationForBigDataDto.setApikey(userOperationLog.getApikey());
                userOperationForBigDataDto.setRequest(userOperationLog.getRequest());
                userOperationForBigDataDto.setResponse(userOperationLog.getResponse());
                userOperationForBigDataDto.setResponseStatus(userOperationLog.getResponseStatus());
                userOperationForBigDataDto.setOperation(userOperationLog.getOperation());
                userOperationForBigDataDto.setUuid(userOperationLog.getUuid());
                //以下数据获取不到
                userOperationForBigDataDto.setFailReason(null);
                userOperationForBigDataDto.setDeviceInfo(null);
                userOperationForBigDataDto.setSessionId(null);
                userOperationForBigDataDto.setReferer(null);
                log.info("send to kafka id [{}]", userOperationLog.getId());
                kafkaTemplate.send(TOPIC, JSON.toJSONString(userOperationForBigDataDto));
                log.info("send to kafka id [{}] end", userOperationLog.getId());
            });
            skip = skip + batchSize;
            log.info("skip [{}]", skip);
        }
        return null;
    }
}
