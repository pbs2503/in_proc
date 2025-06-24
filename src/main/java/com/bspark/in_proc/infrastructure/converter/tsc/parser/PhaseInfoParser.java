package com.bspark.in_proc.infrastructure.converter.tsc.parser;

import com.bspark.in_proc.domain.model.PhaseInfoData;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhaseInfoParser {

    private static final Logger logger = LoggerFactory.getLogger(PhaseInfoParser.class);
    private static final int MIN_DATA_LENGTH = 8;

    private final DataValidator dataValidator;

    @Autowired
    public PhaseInfoParser(DataValidator dataValidator) {
        this.dataValidator = dataValidator;
    }

    public PhaseInfoData parse(byte[] data, int standard) {
        logger.debug("Parsing phase info data with standard: R{}", standard);

        dataValidator.validateDataField(data, MIN_DATA_LENGTH);

        return PhaseInfoData.builder()
                .dataField(data)
                .standard(standard)
                .build();
    }
}