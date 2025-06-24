package com.bspark.in_proc.infrastructure.persistence.redis;

import com.bspark.in_proc.domain.model.TscData;
import com.bspark.in_proc.domain.repository.TscDataRepository;
import com.bspark.in_proc.shared.config.properties.RedisProperties;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RedisTscDataRepository implements TscDataRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisTscDataRepository.class);

    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;

    @Autowired
    public RedisTscDataRepository(StringRedisTemplate redisTemplate,
                                  RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    @Override
    public void save(TscData tscData) {
        String key = buildKey(tscData.getTscId());
        String jsonData = tscData.getJsonData();

        logger.info("=== REDIS SAVE START === TSC: {} Key: {}", tscData.getTscId(), key);
        logger.info("Data size: {} bytes, TTL: {}", jsonData.length(), redisProperties.getDefaultTtl());

        try {
            // Redis 연결 상태 확인
            if (!isRedisHealthy()) {
                throw new DataProcessingException("Redis connection is not healthy");
            }

            // 데이터 저장
            redisTemplate.opsForValue().set(key, jsonData, redisProperties.getDefaultTtl());
            logger.info("Data saved to Redis for key: {}", key);

            // 저장 확인
            String savedData = redisTemplate.opsForValue().get(key);
            boolean exists = redisTemplate.hasKey(key);

            logger.info("=== REDIS SAVE VERIFICATION ===");
            logger.info("Key exists: {}", exists);
            logger.info("Saved data length: {}", savedData != null ? savedData.length() : 0);
            logger.info("Original data length: {}", jsonData.length());

            if (savedData == null || !savedData.equals(jsonData)) {
                throw new DataProcessingException("Data verification failed after save");
            }

            logger.info("=== REDIS SAVE SUCCESS === TSC: {}", tscData.getTscId());

        } catch (Exception e) {
            logger.error("=== REDIS SAVE FAILED === TSC: {} Key: {}", tscData.getTscId(), key, e);
            throw new DataProcessingException("Failed to save TSC data to Redis: " + tscData.getTscId(), e);
        }
    }

    @Override
    public Optional<TscData> findById(String tscId) {
        String key = buildKey(tscId);
        logger.debug("Retrieving TSC data for key: {}", key);

        try {
            String jsonData = redisTemplate.opsForValue().get(key);

            if (jsonData != null) {
                logger.debug("Found TSC data for key: {} [Size: {} bytes]", key, jsonData.length());
                return Optional.of(TscData.builder()
                        .tscId(tscId)
                        .jsonData(jsonData)
                        .build());
            }

            logger.debug("No TSC data found for key: {}", key);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to retrieve TSC data for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String tscId) {
        String key = buildKey(tscId);
        try {
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            logger.debug("TSC data existence check for key: {} = {}", key, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Failed to check existence for key: {}", key, e);
            return false;
        }
    }

    @Override
    public void delete(String tscId) {
        String key = buildKey(tscId);
        try {
            redisTemplate.delete(key);
            logger.debug("Deleted TSC data from Redis with key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete TSC data for key: {}", key, e);
            throw new DataProcessingException("Failed to delete TSC data: " + tscId, e);
        }
    }

    private String buildKey(String tscId) {
        return redisProperties.getKeyPrefix() + tscId.toLowerCase();
    }

    private boolean isRedisHealthy() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
}