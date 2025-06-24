package com.bspark.in_proc.application.monitoring;

import com.bspark.in_proc.infrastructure.persistence.redis.RedisService;
import com.bspark.in_proc.infrastructure.converter.ByteToJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SystemHealthService {

    private static final Logger logger = LoggerFactory.getLogger(SystemHealthService.class);

    private final RedisService redisService;
    private final ByteToJsonConverter byteToJsonConverter;
    private final MemoryMXBean memoryMXBean;

    private final AtomicBoolean isSystemHealthy = new AtomicBoolean(true);
    private volatile String lastHealthCheckTime;

    @Autowired
    public SystemHealthService(RedisService redisService, ByteToJsonConverter byteToJsonConverter) {
        this.redisService = redisService;
        this.byteToJsonConverter = byteToJsonConverter;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    public boolean isSystemHealthy() {
        return isSystemHealthy.get();
    }

    public String getLastHealthCheckTime() {
        return lastHealthCheckTime;
    }

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void performHealthCheck() {
        try {
            logger.debug("Starting system health check");

            boolean redisHealthy = checkRedisHealth();
            boolean memoryHealthy = checkMemoryHealth();
            boolean converterHealthy = checkConverterHealth();

            boolean overallHealth = redisHealthy && memoryHealthy && converterHealthy;
            isSystemHealthy.set(overallHealth);

            lastHealthCheckTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            if (overallHealth) {
                logger.info("System health check passed - Redis: {}, Memory: {}, Converter: {}",
                        redisHealthy, memoryHealthy, converterHealthy);
            } else {
                logger.warn("System health check failed - Redis: {}, Memory: {}, Converter: {}",
                        redisHealthy, memoryHealthy, converterHealthy);
            }

        } catch (Exception e) {
            logger.error("Error during system health check", e);
            isSystemHealthy.set(false);
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void logSystemStatistics() {
        try {
            logConverterStatistics();
            logMemoryStatistics();
        } catch (Exception e) {
            logger.error("Error logging system statistics", e);
        }
    }

    private boolean checkRedisHealth() {
        try {
            return redisService.isHealthy();
        } catch (Exception e) {
            logger.error("Redis health check failed", e);
            return false;
        }
    }

    private boolean checkMemoryHealth() {
        try {
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long used = heapMemoryUsage.getUsed();
            long max = heapMemoryUsage.getMax();

            if (max > 0) {
                double usagePercentage = (double) used / max * 100;
                if (usagePercentage > 90) {
                    logger.warn("High memory usage detected: {:.2f}%", usagePercentage);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Memory health check failed", e);
            return false;
        }
    }

    private boolean checkConverterHealth() {
        try {
            long errorCount = byteToJsonConverter.getErrorCount();
            long successCount = byteToJsonConverter.getSuccessCount();
            long totalCount = errorCount + successCount;

            if (totalCount > 100) { // 최소 100개 처리 후 체크
                double errorRate = (double) errorCount / totalCount * 100;
                if (errorRate > 10) { // 10% 이상 에러율
                    logger.warn("High converter error rate detected: {:.2f}%", errorRate);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Converter health check failed", e);
            return false;
        }
    }

    private void logConverterStatistics() {
        long processed = byteToJsonConverter.getProcessedCount();
        long success = byteToJsonConverter.getSuccessCount();
        long errors = byteToJsonConverter.getErrorCount();
        double successRate = byteToJsonConverter.getSuccessRate();

        logger.info("Converter Statistics - Processed: {}, Success: {}, Errors: {}, Success Rate: {:.2f}%",
                processed, success, errors, successRate);
    }

    private void logMemoryStatistics() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        logger.debug("Memory Statistics - Heap: {} MB used / {} MB max, Non-Heap: {} MB used / {} MB max",
                heapMemoryUsage.getUsed() / 1024 / 1024,
                heapMemoryUsage.getMax() / 1024 / 1024,
                nonHeapMemoryUsage.getUsed() / 1024 / 1024,
                nonHeapMemoryUsage.getMax() / 1024 / 1024);
    }

    public HealthStatus getDetailedHealthStatus() {
        return HealthStatus.builder()
                .isSystemHealthy(isSystemHealthy.get())
                .redisHealthy(checkRedisHealth())
                .memoryHealthy(checkMemoryHealth())
                .converterHealthy(checkConverterHealth())
                .lastCheckTime(lastHealthCheckTime)
                .converterProcessed(byteToJsonConverter.getProcessedCount())
                .converterSuccessRate(byteToJsonConverter.getSuccessRate())
                .memoryUsagePercentage(getMemoryUsagePercentage())
                .build();
    }

    private double getMemoryUsagePercentage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long used = heapMemoryUsage.getUsed();
        long max = heapMemoryUsage.getMax();
        return max > 0 ? (double) used / max * 100 : 0;
    }
}