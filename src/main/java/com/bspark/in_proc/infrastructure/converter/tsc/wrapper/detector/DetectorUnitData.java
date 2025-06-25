package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector;

public class DetectorUnitData {
    public int occupy_time;
    public int non_occupy_time;
    public int traffic_volume;
    public int saturation;
    public int saturation_velocity;
    public int saturation_non_occupy_time;

    public DetectorUnitData(int occupy_time, int non_occupy_time, int traffic_volume, int saturation,
                            int saturation_velocity, int saturation_non_occupy_time) {
        this.occupy_time = occupy_time;
        this.non_occupy_time = non_occupy_time;
        this.traffic_volume = traffic_volume;
        this.saturation = saturation;
        this.saturation_velocity = saturation_velocity;
        this.saturation_non_occupy_time = saturation_non_occupy_time;
    }
}