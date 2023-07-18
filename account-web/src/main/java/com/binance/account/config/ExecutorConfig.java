package com.binance.account.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by Fei.Huang on 2018/12/12.
 *
 * 建议不同业务创建不同的异步执行器，起到线程隔离的作用
 */
@Configuration
@EnableAsync
public class ExecutorConfig {

    private int corePoolSize = 2;
    private int maxPoolSize = 10;
    private int queueCapacity = 1000;

    /**
     * 普通http调用异步执行器
     * 
     * @return
     */
    @Bean
    public Executor simpleRequestAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("simpleRequestAsync-");
        // 拒绝策略：由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 同步手续费TradeLevel异步执行器
     * 
     * @return
     */
    @Bean
    public Executor userInfoAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize * 2);
        executor.setMaxPoolSize(maxPoolSize * 2);
        executor.setQueueCapacity(queueCapacity * 100);
        executor.setThreadNamePrefix("userInfoAsync-");
        // 拒绝策略：由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
