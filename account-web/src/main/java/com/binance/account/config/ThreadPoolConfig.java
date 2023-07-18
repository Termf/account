package com.binance.account.config;

import lombok.extern.log4j.Log4j2;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fei.Huang on 2018/10/18.
 */
@Log4j2
@Configuration
public class ThreadPoolConfig {

    private static final int NUM_OF_PROCESSORS = 2 * Runtime.getRuntime().availableProcessors();

    @Bean
    public ExecutorService getExecutorService() {

        log.info("ThreadPoolConfig getExecutorService NUM_OF_PROCESSORS:{}", NUM_OF_PROCESSORS);

        ThreadFactory accountThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("account-thread-pool-%d").build();

        return new ThreadPoolExecutor(NUM_OF_PROCESSORS, NUM_OF_PROCESSORS, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000), accountThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
}