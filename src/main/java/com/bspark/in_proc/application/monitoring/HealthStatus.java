package com.bspark.in_proc.application.monitoring;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthStatus {
    private final boolean isSystemHealthy;
    private final boolean redisHealthy;
    private final boolean memoryHealthy;
    private final boolean converterHealthy;
    private final String lastCheckTime;
    private final long converterProcessed;
    private final double converterSuccessRate;
    private final double memoryUsagePercentage;

    public String getSummary() {
        return String.format("System: %s, Redis: %s, Memory: %.1f%%, Converter: %.1f%% success",
                isSystemHealthy ? "OK" : "ERROR",
                redisHealthy ? "OK" : "ERROR",
                memoryUsagePercentage,
                converterSuccessRate);
    }
}