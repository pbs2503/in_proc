
package com.bspark.in_proc.infrastructure.converter.standard;

import com.bspark.in_proc.shared.constant.TscConstants;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StandardDetector {
    private static final Logger logger = LoggerFactory.getLogger(StandardDetector.class);

    private final TscProcessingProperties properties;

    @Autowired
    public StandardDetector(TscProcessingProperties properties) {
        this.properties = properties;
    }

    public int detectStandard(byte[] data) {
        logger.debug("Detecting TSC data standard for data length: {}", data != null ? data.length : 0);

        if (data == null || data.length < TscConstants.MIN_DATA_LENGTH) {
            logger.warn("Data is null or too short: {}", data != null ? data.length : 0);
            return TscConstants.STANDARD_UNKNOWN;
        }

        try {
            // R25 표준 검사 (시작 바이트 패턴 확인)
            // R25 표준 검사 (시작 바이트 패턴 확인)
            if (isR25Standard(data)) {
                if (isValidR25Data(data)) {
                    logger.info("Detected R25 standard [Length: {}] [Start bytes: {}]",
                            data.length, String.format("%02X %02X", data[0] & 0xFF, data[1] & 0xFF));

                    return TscConstants.STANDARD_R25;
                } else {
                    logger.warn("Invalid R25 data format detected");
                    return TscConstants.STANDARD_UNKNOWN;
                }
            }

// R27 표준 검사 (시작 바이트 패턴 확인)
            if (isR27Standard(data)) {
                logger.info("Detected R27 standard [Length: {}] [Start bytes: {}]",
                        data.length, String.format("%02X %02X", data[0] & 0xFF, data[1] & 0xFF));
                return TscConstants.STANDARD_R27;
            }

            // 패턴이 일치하지 않으면 길이로 추정
            logger.warn("No start byte pattern matched, estimating by length: {}", data.length);

            if (data.length >= properties.getDataHeader().getR27HeaderSize() * 2 + 23) {
                logger.info("Defaulting to R27 based on data length: {}", data.length);
                return TscConstants.STANDARD_R27;
            } else if (data.length >= properties.getDataHeader().getR25HeaderSize() * 2 + 23) {
                logger.info("Defaulting to R25 based on data length: {}", data.length);
                return TscConstants.STANDARD_R25;
            }

            logger.error("Unable to determine standard for data length: {}", data.length);
            return TscConstants.STANDARD_UNKNOWN;

        } catch (Exception e) {
            logger.error("Error detecting data standard", e);
            return TscConstants.STANDARD_UNKNOWN;
        }
    }

    /**
     * R25 표준 데이터인지 확인 (시작 바이트 패턴)
     */
    private boolean isR25Standard(byte[] data) {
        byte[] r25StartBytes = properties.getDataHeader().getR25StartBytes();

        if (data.length < r25StartBytes.length) {
            return false;
        }

        boolean matches = Arrays.equals(Arrays.copyOf(data, r25StartBytes.length), r25StartBytes);
        logger.debug("R25 start bytes check: {} (expected: {:02X} {:02X}, actual: {:02X} {:02X})",
                matches,
                r25StartBytes[0] & 0xFF, r25StartBytes[1] & 0xFF,
                data[0] & 0xFF, data[1] & 0xFF);

        return matches;
    }

    /**
     * R27 표준 데이터인지 확인 (시작 바이트 패턴)
     */
    private boolean isR27Standard(byte[] data) {
        byte[] r27StartBytes = properties.getDataHeader().getR27StartBytes();

        if (data.length < r27StartBytes.length) {
            return false;
        }

        boolean matches = Arrays.equals(Arrays.copyOf(data, r27StartBytes.length), r27StartBytes);
        logger.debug("R27 start bytes check: {} (expected: {:02X} {:02X}, actual: {:02X} {:02X})",
                matches,
                r27StartBytes[0] & 0xFF, r27StartBytes[1] & 0xFF,
                data[0] & 0xFF, data[1] & 0xFF);

        return matches;
    }

    /**
     * R25 데이터의 유효성 검사
     */
    private boolean isValidR25Data(byte[] data) {
        // R25 데이터의 기본 검증
        if (data.length <= 4) {
            return false;
        }

        // 특정 위치의 바이트가 0이 아닌지 확인
        boolean valid = data[2] != 0x00 && data[4] != 0x00;
        logger.debug("R25 data validation: {} (byte[2]: {:02X}, byte[4]: {:02X})",
                valid, data[2] & 0xFF, data[4] & 0xFF);

        return valid;
    }
}