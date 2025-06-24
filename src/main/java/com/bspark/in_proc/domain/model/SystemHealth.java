package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class SystemHealth {
    private final boolean isSystemHealthy;
    private final boolean redisHealthy;
    private final boolean memoryHealthy;
    private final boolean converterHealthy;
    private final LocalDateTime checkTime;
    private final long converterProcessed;
    private final long converterSuccess;
    private final long converterErrors;
    private final double converterSuccessRate;
    private final double memoryUsagePercentage;

    public String getStatus() {
        return isSystemHealthy ? "HEALTHY" : "UNHEALTHY";
    }

    public String getSummary() {
        return String.format("System: %s, Redis: %s, Memory: %.1f%%, Converter: %.1f%% success",
                isSystemHealthy ? "OK" : "ERROR",
                redisHealthy ? "OK" : "ERROR",
                memoryUsagePercentage,
                converterSuccessRate);
    }
}