package com.bspark.in_proc.interfaces.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "tsc.async")
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    private int corePoolSize = 10;
    private int maxPoolSize = 20;
    private int queueCapacity = 100;
    private int keepAliveSeconds = 60;
    private String threadNamePrefix = "tsc-async-";
    private boolean waitForTasksToCompleteOnShutdown = true;
    private int awaitTerminationSeconds = 30;

    @Bean(name = "asyncConverterExecutor")
    public Executor asyncConverterExecutor() {
        logger.info("Creating async converter executor with core-pool-size: {}, max-pool-size: {}, queue-capacity: {}",
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 설정
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);

        // 종료 시 대기 설정
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // 거부 정책 설정 - 로깅과 함께 호출자에서 실행
        executor.setRejectedExecutionHandler(new LoggingCallerRunsPolicy());

        // 스레드 팩토리 커스터마이징
        executor.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName(threadNamePrefix + thread.getId());
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) ->
                    logger.error("Uncaught exception in async thread {}: {}", t.getName(), e.getMessage(), e));
            return thread;
        });

        executor.initialize();

        logger.info("Async converter executor initialized successfully");
        return executor;
    }

    /**
     * 커스텀 거부 정책 - 로깅 기능 추가
     */
    private static class LoggingCallerRunsPolicy implements RejectedExecutionHandler {
        private static final Logger logger = LoggerFactory.getLogger(LoggingCallerRunsPolicy.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            logger.warn("Task rejected by async executor. Pool size: {}, Active threads: {}, Queue size: {}. Running in caller thread.",
                    executor.getPoolSize(), executor.getActiveCount(), executor.getQueue().size());

            if (!executor.isShutdown()) {
                r.run();
            } else {
                logger.warn("Executor is shutdown, task will be discarded");
            }
        }
    }

    // Getters and Setters for Configuration Properties
    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }

    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }

    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

    public int getKeepAliveSeconds() { return keepAliveSeconds; }
    public void setKeepAliveSeconds(int keepAliveSeconds) { this.keepAliveSeconds = keepAliveSeconds; }

    public String getThreadNamePrefix() { return threadNamePrefix; }
    public void setThreadNamePrefix(String threadNamePrefix) { this.threadNamePrefix = threadNamePrefix; }

    public boolean isWaitForTasksToCompleteOnShutdown() { return waitForTasksToCompleteOnShutdown; }
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public int getAwaitTerminationSeconds() { return awaitTerminationSeconds; }
    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }
}