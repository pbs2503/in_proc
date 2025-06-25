package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector;

public class DetectorUnit {
    public int channel;
    public DetectorUnitStatus detectorUnitStatus;
    public DetectorUnitData detectorUnitData;

    public DetectorUnit(int channel, DetectorUnitStatus detectorUnitStatus, DetectorUnitData detectorUnitData) {
        this.channel = channel;
        this.detectorUnitStatus = detectorUnitStatus;
        this.detectorUnitData = detectorUnitData;
    }
}