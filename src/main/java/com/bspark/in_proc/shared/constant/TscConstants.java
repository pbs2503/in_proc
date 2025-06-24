package com.bspark.in_proc.shared.constant;

public final class TscConstants {

    // TSC Standards
    public static final int STANDARD_R25 = 25;
    public static final int STANDARD_R27 = 27;
    public static final int STANDARD_UNKNOWN = 0;

    // Header Sizes
    public static final int R25_HEADER_SIZE = 5;
    public static final int R27_HEADER_SIZE = 8;

    // OPCODE
    public static final byte OPCODE_STATUS_0x13 = 0x13;
    public static final byte OPCODE_STATUS_0x23 = 0x23;
    public static final byte OPCODE_STATUS_0x33 = 0x33;

    // Data Validation
    public static final int MIN_DATA_LENGTH = 10;
    public static final int MAX_TSC_NAME_LENGTH = 50;

    private TscConstants() {
        // 유틸리티 클래스
    }
}