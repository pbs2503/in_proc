
package com.bspark.in_proc.interfaces.rest;

import com.bspark.in_proc.infrastructure.persistence.redis.RedisService;
import com.bspark.in_proc.infrastructure.converter.ByteToJsonConverter;
import com.bspark.in_proc.application.monitoring.HealthStatus;
import com.bspark.in_proc.application.monitoring.SystemHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ManagementController.class);

    private final SystemHealthService systemHealthService;
    private final ByteToJsonConverter byteToJsonConverter;
    private final RedisService redisService;

    @Autowired
    public ManagementController(SystemHealthService systemHealthService,
                                ByteToJsonConverter byteToJsonConverter,
                                RedisService redisService) {
        this.systemHealthService = systemHealthService;
        this.byteToJsonConverter = byteToJsonConverter;
        this.redisService = redisService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> getHealth() {
        HealthStatus status = systemHealthService.getDetailedHealthStatus();

        if (status.isSystemHealthy()) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.status(503).body(status);
        }
    }

    @GetMapping("/health/simple")
    public ResponseEntity<Map<String, Object>> getSimpleHealth() {
        boolean isHealthy = systemHealthService.isSystemHealthy();

        Map<String, Object> response = Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "timestamp", systemHealthService.getLastHealthCheckTime()
        );

        return isHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = Map.of(
                "converter", Map.of(
                        "processed", byteToJsonConverter.getProcessedCount(),
                        "success", byteToJsonConverter.getSuccessCount(),
                        "errors", byteToJsonConverter.getErrorCount(),
                        "successRate", byteToJsonConverter.getSuccessRate()
                ),
                "redis", Map.of(
                        "healthy", redisService.isHealthy()
                )
        );

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/statistics/reset")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        logger.info("Resetting converter statistics via management API");

        byteToJsonConverter.resetStatistics();

        Map<String, String> response = Map.of(
                "message", "Statistics have been reset successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/redis/status/{tscName}")
    public ResponseEntity<Map<String, Object>> getRedisStatus(@PathVariable String tscName) {
        try {
            boolean exists = redisService.exists(tscName);
            String data = exists ? redisService.getJson(tscName) : null;

            Map<String, Object> response = Map.of(
                    "tscName", tscName,
                    "exists", exists,
                    "dataLength", data != null ? data.length() : 0,
                    "hasData", data != null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting Redis status for TSC: {}", tscName, e);

            Map<String, Object> response = Map.of(
                    "tscName", tscName,
                    "error", e.getMessage()
            );

            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/redis/data/{tscName}")
    public ResponseEntity<Map<String, String>> deleteRedisData(@PathVariable String tscName) {
        try {
            logger.info("Deleting Redis data for TSC: {} via management API", tscName);

            redisService.delete(tscName);

            Map<String, String> response = Map.of(
                    "message", "Data deleted successfully for TSC: " + tscName
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting Redis data for TSC: {}", tscName, e);

            Map<String, String> response = Map.of(
                    "error", e.getMessage()
            );

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/health/check")
    public ResponseEntity<Map<String, String>> triggerHealthCheck() {
        logger.info("Manual health check triggered via management API");

        systemHealthService.performHealthCheck();

        Map<String, String> response = Map.of(
                "message", "Health check completed",
                "status", systemHealthService.isSystemHealthy() ? "HEALTHY" : "UNHEALTHY"
        );

        return ResponseEntity.ok(response);
    }
}