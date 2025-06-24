package com.bspark.in_proc.interfaces.dto.request;

import lombok.Getter;

@Getter
public class DataPayload {
    private String clientId;
    private String data; // Hex string
    private long timestamp;
    private int dataLength;
}
