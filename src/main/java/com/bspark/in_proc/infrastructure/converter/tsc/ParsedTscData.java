package com.bspark.in_proc.infrastructure.converter.tsc;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParsedTscData {
    private final byte[] originalData;
    private final byte[] extractedData;
    private final int standard;
    private final int originalLength;
    private final int extractedLength;
}