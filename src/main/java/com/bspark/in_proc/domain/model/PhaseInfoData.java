package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Data;

/*
실제 Phase Info Data Structure 에 맞춰 수정 필요
*/
@Data
@Builder
public class PhaseInfoData {
    private byte[] dataField;
    private int standard;
    private int originalDataLength;
    private int extractedDataLength;
}