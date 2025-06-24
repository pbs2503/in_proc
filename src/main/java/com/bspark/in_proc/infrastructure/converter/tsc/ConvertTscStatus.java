
package com.bspark.in_proc.infrastructure.converter.tsc;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.bspark.in_proc.infrastructure.converter.tsc.builder.IntersectionStatusJsonBuilder;
import com.bspark.in_proc.infrastructure.converter.tsc.parser.TscStatusParser;
import com.bspark.in_proc.infrastructure.persistence.redis.RedisService;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.infrastructure.converter.Convert;
import com.bspark.in_proc.domain.model.TscStatusData;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class ConvertTscStatus implements Convert {

    private static final Logger logger = LoggerFactory.getLogger(ConvertTscStatus.class);

    private final RedisService redisService;
    private final TscStatusParser statusParser;
    private final IntersectionStatusJsonBuilder jsonBuilder;
    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public ConvertTscStatus(RedisService redisService,
                            TscStatusParser statusParser,
                            IntersectionStatusJsonBuilder jsonBuilder,
                            TscProcessingProperties properties,
                            DataValidator dataValidator) {
        this.redisService = redisService;
        this.statusParser = statusParser;
        this.jsonBuilder = jsonBuilder;
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    @Override
    public void process(String tsc, byte[] data, int standard) {
        Instant startTime = Instant.now();

        logger.info("Starting TSC status conversion [TSC: {}] [Standard: R{}] [Data Length: {}]",
                tsc, standard, data.length);

        try {
            // 입력 검증
            dataValidator.validateTscName(tsc);
            dataValidator.validateRawData(data);

            // 데이터 파싱
            TscStatusData statusData = statusParser.parse(data, standard);

            // JSON 생성
            JSONObject json = jsonBuilder.buildJson(tsc, standard, statusData);

            // Redis 저장
            saveToRedis(tsc, json);

            // 성능 로깅
            Duration processingTime = Duration.between(startTime, Instant.now());
            logger.info("TSC status conversion completed [TSC: {}] [Processing Time: {}ms]",
                    tsc, processingTime.toMillis());

            // 디버그용 Pretty JSON 출력 (R25만)
            if (shouldLogPrettyJson(standard)) {
                logger.info("TSC Status JSON for {} -> {}", tsc,
                        json.toJSONString(JSONWriter.Feature.PrettyFormat));
            }

        } catch (DataProcessingException e) {
            logger.error("Data processing failed for TSC: {} with standard R{}", tsc, standard, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during TSC status conversion for TSC: {}", tsc, e);
            throw new DataProcessingException("Failed to convert TSC status for: " + tsc, e);
        }
    }

    private void saveToRedis(String tsc, JSONObject json) {
        try {
            // Redis 연결 상태 체크 추가
            if (!redisService.isHealthy()) {
                logger.error("Redis is not healthy - cannot save data for TSC: {}", tsc);
                throw new DataProcessingException("Redis connection is not healthy for TSC: " + tsc);
            }

            String jsonString = json.toJSONString();
            logger.info("Attempting to save to Redis [TSC: {}] [Size: {} bytes]", tsc, jsonString.length());

            redisService.saveJson(tsc, jsonString);

            // 저장 후 확인
            boolean exists = redisService.exists(tsc);
            logger.info("Save verification - Data exists in Redis for TSC {}: {}", tsc, exists);

            logger.debug("Successfully saved TSC status to Redis [TSC: {}] [Size: {} bytes]",
                    tsc, jsonString.length());
        } catch (Exception e) {
            logger.error("Failed to save TSC status to Redis for TSC: {}", tsc, e);
            throw new DataProcessingException("Failed to save to Redis for TSC: " + tsc, e);
        }
    }

    private boolean shouldLogPrettyJson(int standard) {
        return properties.getLogging().isEnablePrettyJson() &&
                standard == properties.getStandard().getR25();
    }
}