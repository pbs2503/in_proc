package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

public class DetectorInfoWrapper {

    @JSONField(name = "detectors")
    public List<DetectorUnit> detectors;

    public DetectorInfoWrapper(List<DetectorUnit> detectors) {
        this.detectors = detectors;
    }

}





