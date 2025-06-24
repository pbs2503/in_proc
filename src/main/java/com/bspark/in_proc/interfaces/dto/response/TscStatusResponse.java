package com.bspark.in_proc.interfaces.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TscStatusResponse {
    private boolean success;
    private String tscId;
    private String message;
    private String data;
    private String timestamp;
}