package com.binance.account.service.bigdata;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;

@Component
@Log4j2
public class AsyncBigDataProducer {

    @Resource(name = "bigDataKafkaTemplate")
    private KafkaTemplate bigDataKafkaTemplate;

    @Async
    public void produceMsgToBigData(String topic, Object body) {
        try {
            if (bigDataKafkaTemplate != null && StringUtils.isNotBlank(topic)) {
                log.info("produceMsgToBigData produces: {}", JSON.toJSONString(body));
                ListenableFuture<SendResult> sendResultFuture = bigDataKafkaTemplate.send(topic, JSON.toJSONString(body));
                sendResultFuture.addCallback(new ListenableFutureCallback<SendResult>() {

                    @Override
                    public void onSuccess(SendResult result) {
                        log.info("produceMsgToBigData() send result success");
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.warn("produceMsgToBigData() send result failed", ex);
                    }
                });
            } else {
                log.info("kafka not set.");
            }
        } catch (Exception e) {
            log.error("produceMsgToBigData() error.", e);
        }
    }

}
