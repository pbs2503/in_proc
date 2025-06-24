package com.bspark.in_proc.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TscDataRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Type is required")
    private String type;  // INTERSECTION_STATUS 등의 타입

    @NotBlank(message = "Data is required")
    private String data;

    @NotNull(message = "Timestamp is required")
    private Long timestamp;

    @NotNull(message = "Data length is required")
    private Integer dataLength;
}