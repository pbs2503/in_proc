package com.bspark.in_proc.domain.repository;

import com.bspark.in_proc.domain.model.SystemHealth;

public interface SystemMetricsRepository {
    void saveMetrics(SystemHealth health);
    SystemHealth getLatestMetrics();
    void incrementProcessedCount();
    void incrementSuccessCount();
    void incrementErrorCount();
    long getProcessedCount();
    long getSuccessCount();
    long getErrorCount();
    void resetCounters();
}