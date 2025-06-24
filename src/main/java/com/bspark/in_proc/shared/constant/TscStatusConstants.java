package com.bspark.in_proc.shared.constant;

import java.util.Map;

public final class TscStatusConstants {

    public static final Map<Integer, String> OPERATION_MODE_MAP = Map.of(
            0, "SCU-MODE",
            1, "OFFLINE-MODE",
            2, "ACT-OFFLINE-MODE",
            4, "ACT-ONLINE-MODE",
            5, "ONLINE-MODE"
    );

    public static final Map<Integer, String> OPERATION_MAP = Map.of(
            0, "Normal",
            1, "TOD #1",
            2, "TOD #2",
            3, "TOD #3",
            4, "TOD #4",
            5, "TOD #5",
            6, "PEDESTRIAN",
            7, "RESERVE"
    );

    private TscStatusConstants() {
        // 유틸리티 클래스
    }

    public static String getStatus(int value, int mask) {
        return (value & mask) == 0 ? "OK" : "ERROR";
    }

    public static String getEnabledStatus(int value, int mask) {
        return (value & mask) == 0 ? "DISABLED" : "ENABLED";
    }

    public static String getOnOff(int value, int mask) {
        return (value & mask) == 0 ? "OFF" : "ON";
    }

    public static String getOperationMode(int value) {
        return OPERATION_MODE_MAP.getOrDefault(value & 0b111, "UNKNOWN");
    }

    public static String getOperationMap(int value) {
        return OPERATION_MAP.getOrDefault((value & 0b01110000) >> 4, "UNKNOWN");
    }
}