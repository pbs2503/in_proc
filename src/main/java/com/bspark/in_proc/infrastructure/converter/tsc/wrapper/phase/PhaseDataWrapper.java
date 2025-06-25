package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.phase;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;

import java.util.List;

@Getter
public class PhaseDataWrapper {

    private PhaseDataLine pedestrian;
    private PhaseDataLine vehicle;

    public PhaseDataWrapper(PhaseDataLine pedestrian, PhaseDataLine vehicle){
        this.pedestrian = pedestrian;
        this.vehicle = vehicle;
    }
}
