package com.bspark.in_proc.domain.model;

import lombok.Builder;
import lombok.Data;
/*
실제 Detector Info Data Structure 에 맞춰 수정 필요
*/
@Data
@Builder
public class DetectorInfoData {
    private byte[] dataField;
    private int standard;
    private int originalDataLength;
    private int extractedDataLength;
}