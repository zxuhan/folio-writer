package com.zxuhan.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async task configuration.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool for async article generation tasks.
     */
    @Bean(name = "articleExecutor")
    public Executor articleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core thread count
        executor.setCorePoolSize(5);

        // Maximum thread count
        executor.setMaxPoolSize(10);

        // Queue capacity
        executor.setQueueCapacity(100);

        // Thread name prefix
        executor.setThreadNamePrefix("article-async-");

        // Rejection policy: run in caller thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for all tasks to complete before shutting down
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Shutdown wait time (seconds)
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        return executor;
    }
}
