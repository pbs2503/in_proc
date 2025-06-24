package com.bspark.in_proc.shared.util;

import com.bspark.in_proc.shared.exception.InvalidDataFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public final class Utility {

    private static final Logger logger = LoggerFactory.getLogger(Utility.class);

    // 성능 최적화를 위한 StringBuilder 재사용을 위한 ThreadLocal
    private static final ThreadLocal<StringBuilder> STRING_BUILDER_CACHE =
            ThreadLocal.withInitial(() -> new StringBuilder(1024));

    private Utility() {
        // 유틸리티 클래스
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     * @param data 변환할 바이트 배열
     * @return 16진수 문자열 (공백으로 구분)
     */
    public static String toHexString(byte[] data) {
        if (data == null) {
            return "null";
        }

        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = STRING_BUILDER_CACHE.get();
        sb.setLength(0); // 기존 내용 지우기

        try {
            for (int i = 0; i < data.length; i++) {
                sb.append(String.format("%02X", data[i] & 0xFF));
                if (i < data.length - 1) {
                    sb.append(" ");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Error converting byte array to hex string", e);
            return "Error converting data";
        }
    }

    /**
     * 압축된 16진수 문자열 (공백 없음)
     * @param data 변환할 바이트 배열
     * @return 압축된 16진수 문자열
     */
    public static String toCompactHexString(byte[] data) {
        if (data == null) {
            return "null";
        }

        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = STRING_BUILDER_CACHE.get();
        sb.setLength(0);

        try {
            for (byte b : data) {
                sb.append(String.format("%02X", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Error converting byte array to compact hex string", e);
            return "Error";
        }
    }

    /**
     * 16진수 문자열을 바이트 배열로 변환
     * @param hexString 16진수 문자열 (공백 포함 가능)
     * @return 바이트 배열
     * @throws InvalidDataFormatException 잘못된 형식의 경우
     */
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            throw new InvalidDataFormatException("Hex string cannot be null");
        }

        // 공백 및 특수 문자 제거
        String cleanHex = hexString.replaceAll("[\\s:-]", "").toUpperCase();

        if (cleanHex.isEmpty()) {
            return new byte[0];
        }

        if (cleanHex.length() % 2 != 0) {
            throw new InvalidDataFormatException("Hex string must have even length: " + hexString);
        }

        try {
            int len = cleanHex.length();
            byte[] data = new byte[len / 2];

            for (int i = 0; i < len; i += 2) {
                int high = Character.digit(cleanHex.charAt(i), 16);
                int low = Character.digit(cleanHex.charAt(i + 1), 16);

                if (high == -1 || low == -1) {
                    throw new InvalidDataFormatException(
                            "Invalid hex character in string: " + hexString);
                }

                data[i / 2] = (byte) ((high << 4) + low);
            }

            return data;
        } catch (Exception e) {
            throw new InvalidDataFormatException("Failed to parse hex string: " + hexString, e);
        }
    }

    /**
     * 바이트 배열의 특정 범위를 16진수 문자열로 변환
     * @param data 바이트 배열
     * @param offset 시작 위치
     * @param length 길이
     * @return 16진수 문자열
     */
    public static String toHexString(byte[] data, int offset, int length) {
        if (data == null) {
            return "null";
        }

        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException(
                    String.format("Invalid range: offset=%d, length=%d, array length=%d",
                            offset, length, data.length));
        }

        if (length == 0) {
            return "";
        }

        StringBuilder sb = STRING_BUILDER_CACHE.get();
        sb.setLength(0);

        try {
            for (int i = 0; i < length; i++) {
                sb.append(String.format("%02X", data[offset + i] & 0xFF));
                if (i < length - 1) {
                    sb.append(" ");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Error converting byte array range to hex string", e);
            return "Error converting data";
        }
    }

    /**
     * 바이트 값을 안전하게 unsigned int로 변환
     * @param b 바이트 값
     * @return 0-255 범위의 int 값
     */
    public static int toUnsignedInt(byte b) {
        return b & 0xFF;
    }

    /**
     * 디버깅을 위한 바이트 배열 정보 출력
     * @param data 바이트 배열
     * @param description 설명
     * @return 포맷된 정보 문자열
     */
    public static String formatDataInfo(byte[] data, String description) {
        if (data == null) {
            return String.format("[%s] null data", description);
        }

        return String.format("[%s] Length: %d, Data: %s",
                description, data.length,
                data.length <= 50 ? toHexString(data) :
                        toHexString(data, 0, 25) + " ... " +
                                toHexString(data, data.length - 25, 25));
    }

    /**
     * 성능 테스트를 위한 랜덤 바이트 배열 생성
     * @param length 배열 길이
     * @return 랜덤 바이트 배열
     */
    public static byte[] generateRandomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }

        byte[] data = new byte[length];
        ThreadLocalRandom.current().nextBytes(data);
        return data;
    }
}