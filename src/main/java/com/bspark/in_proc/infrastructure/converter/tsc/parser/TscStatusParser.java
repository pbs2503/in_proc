package com.bspark.in_proc.infrastructure.converter.tsc.parser;

import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.domain.model.TscStatusData;
import com.bspark.in_proc.shared.exception.UnsupportedStandardException;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TscStatusParser {

    private static final Logger logger = LoggerFactory.getLogger(TscStatusParser.class);
    private static final int MIN_DATA_FIELD_LENGTH = 23; // 최소 필요한 데이터 필드 길이

    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public TscStatusParser(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public TscStatusData parse(byte[] data, int standard) {
        logger.debug("Parsing TSC status data with standard: R{}", standard);

        try {
            byte[] dataField = extractDataField(data, standard);
            dataValidator.validateDataField(dataField, MIN_DATA_FIELD_LENGTH);

            TscStatusData result = TscStatusData.builder()
                    .dataField(dataField)
                    .standard(standard)
                    .originalDataLength(data.length)
                    .extractedDataLength(dataField.length)
                    .build();

            logger.debug("Successfully parsed TSC status data. Original length: {}, Extracted length: {}",
                    data.length, dataField.length);

            return result;
        } catch (Exception e) {
            logger.error("Failed to parse TSC status data with standard R{}", standard, e);
            throw e;
        }
    }

    private byte[] extractDataField(byte[] data, int standard) {
        int headSize = getHeaderSize(standard);

        if (data.length < headSize * 2) {
            throw new UnsupportedStandardException(
                    String.format("Data length (%d) is insufficient for standard R%d (requires minimum %d)",
                            data.length, standard, headSize * 2));
        }

        return Arrays.copyOfRange(data, headSize, data.length - headSize);
    }

    private int getHeaderSize(int standard) {
        if (standard == properties.getStandard().getR25()) {
            return properties.getDataHeader().getR25HeaderSize();
        } else if (standard == properties.getStandard().getR27()) {
            return properties.getDataHeader().getR27HeaderSize();
        } else {
            throw new UnsupportedStandardException(standard);
        }
    }
}