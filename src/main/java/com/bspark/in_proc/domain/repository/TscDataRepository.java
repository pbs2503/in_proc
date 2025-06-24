package com.bspark.in_proc.domain.repository;

import com.bspark.in_proc.domain.model.TscData;
import java.util.Optional;

public interface TscDataRepository {
    void save(TscData tscData);
    Optional<TscData> findById(String tscId);
    boolean exists(String tscId);
    void delete(String tscId);
}