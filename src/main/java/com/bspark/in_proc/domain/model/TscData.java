package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class TscData {
    private final String tscId;
    private final String messageType;
    private final int standard;
    private final String jsonData;
    private final byte[] rawData;
    private final LocalDateTime timestamp;
    private final int dataLength;

    public boolean isValidStandard() {
        return standard == 25 || standard == 27;
    }

    public String getStandardName() {
        return "R" + standard;
    }
}