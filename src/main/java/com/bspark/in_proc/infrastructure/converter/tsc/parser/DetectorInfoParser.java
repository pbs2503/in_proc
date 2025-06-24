package com.bspark.in_proc.infrastructure.converter.tsc.parser;

import com.bspark.in_proc.domain.model.DetectorInfoData;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DetectorInfoParser {

    private static final Logger logger = LoggerFactory.getLogger(DetectorInfoParser.class);
    private static final int MIN_DATA_LENGTH = 10;

    private final DataValidator dataValidator;

    @Autowired
    public DetectorInfoParser(DataValidator dataValidator) {
        this.dataValidator = dataValidator;
    }

    public DetectorInfoData parse(byte[] data, int standard) {
        logger.debug("Parsing detector info data with standard: R{}", standard);

        dataValidator.validateDataField(data, MIN_DATA_LENGTH);

        return DetectorInfoData.builder()
                .dataField(data)
                .standard(standard)
                .build();
    }
}