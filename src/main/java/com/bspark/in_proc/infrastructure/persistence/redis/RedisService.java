package com.bspark.in_proc.infrastructure.persistence.redis;

import com.bspark.in_proc.shared.config.properties.RedisProperties;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Objects;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;

    @Autowired
    public RedisService(StringRedisTemplate redisTemplate, RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    public void saveJson(String tscName, String jsonString) {
        saveJson(tscName, jsonString, redisProperties.getDefaultTtl());
    }

    public void saveJson(String tscName, String jsonString, Duration ttl) {
        validateInputs(tscName, jsonString);

        String key = buildKey(tscName);
        int retryCount = 0;

        logger.info("Starting to save JSON data for TSC: {} with key: {}", tscName, key);

        while (retryCount <= redisProperties.getMaxRetries()) {
            try {
                logger.info("Save attempt {}/{} for TSC: {}", retryCount + 1, redisProperties.getMaxRetries() + 1, tscName);

                if (ttl != null && !ttl.isZero()) {
                    redisTemplate.opsForValue().set(key, jsonString, ttl);
                    logger.info("Set with TTL: {} for key: {}", ttl, key);
                } else {
                    redisTemplate.opsForValue().set(key, jsonString);
                    logger.info("Set without TTL for key: {}", key);
                }

                // 저장 직후 확인
                String savedValue = redisTemplate.opsForValue().get(key);
                logger.info("Verification - Retrieved value length: {} for key: {}",
                        savedValue != null ? savedValue.length() : 0, key);

                logger.debug("Successfully saved JSON data for TSC: {} with key: {}", tscName, key);
                return;

            } catch (Exception e) {
                retryCount++;
                logger.error("Failed to save JSON data (attempt {}/{}): {}",
                        retryCount, redisProperties.getMaxRetries() + 1, e.getMessage(), e);

                if (retryCount > redisProperties.getMaxRetries()) {
                    throw new DataProcessingException(
                            String.format("Failed to save JSON data for TSC '%s' after %d attempts",
                                    tscName, retryCount), e);
                }

                try {
                    Thread.sleep(redisProperties.getRetryDelay().toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DataProcessingException("Thread interrupted during Redis retry", ie);
                }
            }
        }
    }

    public String getJson(String tscName) {
        if (!StringUtils.hasText(tscName)) {
            throw new IllegalArgumentException("TSC name cannot be empty");
        }

        String key = buildKey(tscName);

        try {
            String result = redisTemplate.opsForValue().get(key);
            logger.debug("Retrieved JSON data for TSC: {} with key: {}", tscName, key);
            return result;
        } catch (Exception e) {
            logger.error("Failed to retrieve JSON data for TSC: {}", tscName, e);
            throw new DataProcessingException(
                    String.format("Failed to retrieve JSON data for TSC '%s'", tscName), e);
        }
    }

    public boolean exists(String tscName) {
        if (!StringUtils.hasText(tscName)) {
            return false;
        }

        try {
            String key = buildKey(tscName);
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("Failed to check existence for TSC: {}", tscName, e);
            return false;
        }
    }

    public void delete(String tscName) {
        if (!StringUtils.hasText(tscName)) {
            throw new IllegalArgumentException("TSC name cannot be empty");
        }

        try {
            String key = buildKey(tscName);
            redisTemplate.delete(key);
            logger.debug("Deleted data for TSC: {} with key: {}", tscName, key);
        } catch (Exception e) {
            logger.error("Failed to delete data for TSC: {}", tscName, e);
            throw new DataProcessingException(
                    String.format("Failed to delete data for TSC '%s'", tscName), e);
        }
    }

    public boolean isHealthy() {
        if (!redisProperties.isEnableHealthCheck()) {
            return true;
        }

        try {
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .ping();
            return true;
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }

    private String buildKey(String tscName) {
        return redisProperties.getKeyPrefix() + tscName.toLowerCase();
    }

    private void validateInputs(String tscName, String jsonString) {
        if (!StringUtils.hasText(tscName)) {
            throw new IllegalArgumentException("TSC name cannot be empty");
        }
        if (!StringUtils.hasText(jsonString)) {
            throw new IllegalArgumentException("JSON string cannot be empty");
        }
    }
}