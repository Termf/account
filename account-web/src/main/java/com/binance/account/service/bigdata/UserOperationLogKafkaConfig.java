package com.binance.account.service.bigdata;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class UserOperationLogKafkaConfig {

    @Value("${bigdata.kafka.producer.bootstrapServers:}")
    private String producerBootstrapServers; //生产者连接Server地址

    @Value("${bigdata.kafka.producer.retries:1}")
    private String producerRetries; //生产者重试次数

    @Value("${bigdata.kafka.producer.batchSize:16384}")
    private String producerBatchSize;

    @Value("${bigdata.kafka.producer.lingerMs:1}")
    private String producerLingerMs;

    @Value("${bigdata.kafka.producer.bufferMemory:33554432}")
    private String producerBufferMemory;

    @Value("${bigdata.kafka.producer.maxBlockMsConfig:1000}")
    private String maxBlockMs;


    /**
     * ProducerFactory
     * @return
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory(){
        if (StringUtils.isBlank(producerBootstrapServers)) {
            return null;
        }
        Map<String, Object> configs = new HashMap<>(); //参数
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        configs.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, producerBatchSize);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, producerLingerMs);
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerBufferMemory);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    /**
     * KafkaTemplate
     * @param
     * @return
     */
    @Bean("bigDataKafkaTemplate")
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        ProducerFactory<Object, Object> factory = producerFactory();
        if (factory != null) {
            return new KafkaTemplate<>(producerFactory(), true);
        } else {
            return null;
        }
    }



}