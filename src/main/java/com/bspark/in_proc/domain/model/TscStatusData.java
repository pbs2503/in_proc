package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TscStatusData {
    private final byte[] dataField;
    private final int standard;
    private final int originalDataLength;
    private final int extractedDataLength;

    public boolean isR25Standard() {
        return standard == 25;
    }

    public boolean isR27Standard() {
        return standard == 27;
    }

    public String getStandardName() {
        return "R" + standard;
    }
}