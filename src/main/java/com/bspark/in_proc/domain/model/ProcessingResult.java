package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProcessingResult {
    private final boolean success;
    private final TscData data;
    private final String errorMessage;
    private final long processingTimeMs;

    public static ProcessingResult success(TscData data) {
        return ProcessingResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ProcessingResult failure(String errorMessage) {
        return ProcessingResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}