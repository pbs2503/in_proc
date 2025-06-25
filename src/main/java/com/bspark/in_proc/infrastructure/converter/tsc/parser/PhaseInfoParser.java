package com.bspark.in_proc.infrastructure.converter.tsc.parser;

import com.bspark.in_proc.domain.model.PhaseInfoData;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.UnsupportedStandardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PhaseInfoParser {

    private static final Logger logger = LoggerFactory.getLogger(PhaseInfoParser.class);
    private static final int MIN_DATA_FIELD_LENGTH = 16;

    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public PhaseInfoParser(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public PhaseInfoData parse(byte[] data, int standard) {
        logger.debug("Parsing phase info data with standard: R{}", standard);

        try{
            byte[] dataField = extractDataField(data, standard);
            dataValidator.validateDataField(dataField, MIN_DATA_FIELD_LENGTH);

            PhaseInfoData result = PhaseInfoData.builder()
                    .dataField(dataField)
                    .standard(standard)
                    .originalDataLength(data.length)
                    .extractedDataLength(dataField.length)
                    .build();

            logger.debug("Successfully parsed Phase info data. Original length: {}, Extracted length: {}",
                    data.length, dataField.length);

            return result;
        } catch (Exception e) {
            logger.error("Failed to parse phase info data with standard R{}", standard, e);
            throw e;
        }
    }

    private byte[] extractDataField(byte[] data, int standard) {
        int headSize = getHeaderSize(standard);
        int tailSize = getTailSize(standard);

        if (data.length < headSize * 2) {
            throw new UnsupportedStandardException(
                    String.format("Data length (%d) is insufficient for standard R%d (requires minimum %d)",
                            data.length, standard, headSize * 2));
        }

        return Arrays.copyOfRange(data, headSize, data.length - tailSize);
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

    private int getTailSize(int standard){
        if (standard == properties.getStandard().getR25()) {
            return properties.getDataTail().getR25TailSize();
        } else if (standard == properties.getStandard().getR27()) {
            return properties.getDataTail().getR27TailSize();
        } else {
            throw new UnsupportedStandardException(standard);
        }
    }
}