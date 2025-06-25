package com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector;

import com.alibaba.fastjson2.annotation.JSONField;

public class DetectorUnitStatus {
    public int volume_error;
    public int occupy_error;
    public int non_occupy_error;
    public int oscillation;
    public int shortField;
    public int open;
    public int installed;

    @JSONField(name = "short")
    public int getShortField() {
        return shortField;
    }

    @JSONField(name = "short")
    public void setShortField(int value) {
        this.shortField = value;
    }

    public DetectorUnitStatus(int volume_error, int occupy_error, int non_occupy_error, int oscillation, int shortField, int open, int installed) {
        this.volume_error = volume_error;
        this.occupy_error = occupy_error;
        this.non_occupy_error = non_occupy_error;
        this.oscillation = oscillation;
        this.shortField = shortField;
        this.open = open;
        this.installed = installed;
    }
}