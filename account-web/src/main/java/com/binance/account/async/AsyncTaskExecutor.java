package com.binance.account.async;


import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.log4j.Log4j2;

/**
 * 异步执行器    （WARNING：服务重启或机器crash，可能会丢失队列中的任务，不建议在敏感业务中使用）
 * @author caixinning
 * @date 2018/05/09 16:09
 **/
@Log4j2
public class AsyncTaskExecutor {

    private final static int queueCapacity = 100000;
    private final static int corePoolSize = 2;
    private final static int maxPoolSize = 20;
    private final static int keepAliveSeconds = 60;

    private static volatile ThreadPoolTaskExecutor taskPool;

    static{
        taskPool = createExecutor();
        log.info("异步服务线程池配置成功...");
    }

    /**
     * 执行异步任务
     */
    public static void execute(Runnable task){
        try {
            taskPool.execute(task);
            logQueueSize();
        } catch (Exception e) {
            log.error("异步任务启动失败！",e);
        }
    }

    public static <V> Future<V> submit(Callable<V> callable){
        Future<V> future = taskPool.submit(callable);
        logQueueSize();
        return future;
    }


    private static void logQueueSize(){
        int queueSize = taskPool.getThreadPoolExecutor().getQueue().size();
        if (queueSize>0 && queueSize%100==0){
            log.info("AsyncTaskExecutor 队列长度:{}，队列已使用{}%",queueSize,(100f*queueSize)/queueCapacity);
        }
    }


    private static ThreadPoolTaskExecutor createExecutor(){
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setQueueCapacity(queueCapacity);
        pool.setCorePoolSize(corePoolSize);
        pool.setMaxPoolSize(maxPoolSize);
        pool.setKeepAliveSeconds(keepAliveSeconds);
        pool.initialize();
        return pool;
    }

}
