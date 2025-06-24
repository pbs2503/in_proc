
package com.bspark.in_proc.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "tsc.redis")
public class RedisProperties {

    private String keyPrefix = "tsc:status:";
    private Duration defaultTtl = Duration.ofHours(24);
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofMillis(100);
    private boolean enableHealthCheck = true;
    private Duration healthCheckInterval = Duration.ofMinutes(5);
}