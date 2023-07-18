package com.binance.account.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * kafka 消费端配置，与现有kafka用法保持一致
 *
 */
@Slf4j
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@Conditional({KafkaExclusionCondition.class})
public class KafkaConsumerConfigure {
    @Value("${bigdata.kafka.consumer.servers}")
    private String servers;
    @Value("${bigdata.kafka.consumer.enable.auto.commit:false}")
    private boolean enableAutoCommit;
    @Value("${bigdata.kafka.consumer.session.timeout:30000}")
    private String sessionTimeout;
    @Value("${bigdata.kafka.consumer.auto.commit.interval:1000}")
    private String autoCommitInterval;
    @Value("${bigdata.kafka.consumer.polltimeout:2000}")
    private int pollTimeOut;
    @Value("${bigdata.kafka.consumer.ack-mode:MANUAL}")
    private ContainerProperties.AckMode ackmode;
    @Value("${bigdata.kafka.consumer.group.id:account-bigdata}")
    private String groupId;
    @Value("${bigdata.kafka.consumer.auto.offset.reset:latest}")
    private String autoOffsetReset;
    @Value("${bigdata.kafka.consumer.concurrency:3}")
    private int concurrency;
    @Value("${bigdata.kafka.consumer.fetch-max-wait:30000}")
    private int fetchMaxWait;
    @Value("${bigdata.kafka.consumer.max-poll-records:5}")
    private int maxPollRecords;
    @Value("${bigdata.kafka.consumer.partition.assignment.strategy:org.apache.kafka.clients.consumer.RoundRobinAssignor}")
    private String strategy;


    @Bean
    @ConditionalOnMissingBean(KafkaListenerContainerFactory.class)
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setPollTimeout(pollTimeOut);
        factory.getContainerProperties().setAckMode(ackmode);
        return factory;
    }

    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }


    public Map<String, Object> consumerConfigs() {
        if (StringUtils.isBlank(servers)) {
            log.error("kafka consumer,servers is null");
        }
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        propsMap.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWait);
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        propsMap.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, strategy);
        return propsMap;
    }


    @Configuration
    @EnableKafka
    @ConditionalOnMissingBean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    protected static class EnableKafkaConfiguration {

    }
}
