package com.bspark.in_proc.infrastructure.converter.tsc.parser;

import com.bspark.in_proc.infrastructure.converter.tsc.ParsedTscData;
import com.bspark.in_proc.shared.constant.TscConstants;
import com.bspark.in_proc.shared.exception.InvalidDataFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TscDataParser {

    private static final Logger logger = LoggerFactory.getLogger(TscDataParser.class);

    public ParsedTscData parse(byte[] rawData, int standard) {
        logger.debug("Parsing TSC data with standard: R{}", standard);

        try {
            byte[] extractedData = extractDataByStandard(rawData, standard);

            ParsedTscData parsedData = ParsedTscData.builder()
                    .originalData(rawData)
                    .extractedData(extractedData)
                    .standard(standard)
                    .originalLength(rawData.length)
                    .extractedLength(extractedData.length)
                    .build();

            logger.debug("Successfully parsed TSC data. Original length: {}, Extracted length: {}",
                    rawData.length, extractedData.length);

            return parsedData;

        } catch (Exception e) {
            logger.error("Failed to parse TSC data with standard R{}", standard, e);
            throw new InvalidDataFormatException("Failed to parse TSC data", e);
        }
    }

    private byte[] extractDataByStandard(byte[] rawData, int standard) {
        int headerSize = getHeaderSize(standard);

        if (rawData.length <= headerSize) {
            throw new InvalidDataFormatException(
                    String.format("Data too short for R%d standard. Required: %d, Actual: %d",
                            standard, headerSize + 1, rawData.length));
        }

        // 헤더를 제외한 데이터 추출
        byte[] extractedData = new byte[rawData.length - headerSize];
        System.arraycopy(rawData, headerSize, extractedData, 0, extractedData.length);

        return extractedData;
    }

    private int getHeaderSize(int standard) {
        return switch (standard) {
            case TscConstants.STANDARD_R25 -> TscConstants.R25_HEADER_SIZE;
            case TscConstants.STANDARD_R27 -> TscConstants.R27_HEADER_SIZE;
            default -> throw new InvalidDataFormatException("Unknown standard: R" + standard);
        };
    }
}