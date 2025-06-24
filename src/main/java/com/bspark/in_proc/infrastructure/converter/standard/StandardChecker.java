package com.bspark.in_proc.infrastructure.converter.standard;

import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.InvalidDataFormatException;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StandardChecker {

    private static final Logger logger = LoggerFactory.getLogger(StandardChecker.class);

    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public StandardChecker(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public int checkDataStandard(byte[] data) {
        dataValidator.validateRawData(data);

        try {
            if (isR25Standard(data)) {
                if (isValidR25Data(data)) {
                    logger.debug("Detected R25 standard data");
                    return properties.getStandard().getR25();
                } else {
                    logger.warn("Invalid R25 data format detected");
                    return properties.getStandard().getUnknown();
                }
            } else if (isR27Standard(data)) {
                logger.debug("Detected R27 standard data");
                return properties.getStandard().getR27();
            } else {
                logger.warn("Unknown data format, defaulting to R27");
                return properties.getStandard().getR27();
            }
        } catch (Exception e) {
            logger.error("Error checking data standard", e);
            throw new InvalidDataFormatException("Failed to determine data standard", e);
        }
    }

    private boolean isR25Standard(byte[] data) {
        byte[] r25StartBytes = properties.getDataHeader().getR25StartBytes();
        return data.length >= r25StartBytes.length &&
                Arrays.equals(Arrays.copyOf(data, r25StartBytes.length), r25StartBytes);
    }

    private boolean isR27Standard(byte[] data) {
        byte[] r27StartBytes = properties.getDataHeader().getR27StartBytes();
        return data.length >= r27StartBytes.length &&
                Arrays.equals(Arrays.copyOf(data, r27StartBytes.length), r27StartBytes);
    }

    private boolean isValidR25Data(byte[] data) {
        return data.length > 4 && data[2] != 0x00 && data[4] != 0x00;
    }
}