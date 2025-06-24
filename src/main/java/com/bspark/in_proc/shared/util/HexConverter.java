package com.bspark.in_proc.shared.util;

import com.bspark.in_proc.shared.exception.InvalidDataException;

public final class HexConverter {

    private static final ThreadLocal<StringBuilder> STRING_BUILDER_CACHE =
            ThreadLocal.withInitial(() -> new StringBuilder(1024));

    private HexConverter() {
        // 유틸리티 클래스
    }

    public static String toHexString(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        StringBuilder sb = STRING_BUILDER_CACHE.get();
        sb.setLength(0);

        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02X", data[i] & 0xFF));
            if (i < data.length - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            throw new InvalidDataException("Hex string cannot be null");
        }

        String cleanHex = hexString.replaceAll("[\\s:-]", "").toUpperCase();

        if (cleanHex.isEmpty()) {
            return new byte[0];
        }

        if (cleanHex.length() % 2 != 0) {
            throw new InvalidDataException("Hex string must have even length");
        }

        try {
            int len = cleanHex.length();
            byte[] data = new byte[len / 2];

            for (int i = 0; i < len; i += 2) {
                int high = Character.digit(cleanHex.charAt(i), 16);
                int low = Character.digit(cleanHex.charAt(i + 1), 16);

                if (high == -1 || low == -1) {
                    throw new InvalidDataException("Invalid hex character in string");
                }

                data[i / 2] = (byte) ((high << 4) + low);
            }

            return data;
        } catch (Exception e) {
            throw new InvalidDataException("Failed to parse hex string", e);
        }
    }
}