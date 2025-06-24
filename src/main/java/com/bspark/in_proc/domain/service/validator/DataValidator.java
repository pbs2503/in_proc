package com.bspark.in_proc.domain.service.validator;

import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.InvalidDataFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DataValidator {

    private final TscProcessingProperties properties;

    @Autowired
    public DataValidator(TscProcessingProperties properties) {
        this.properties = properties;
    }

    public void validateTscName(String tsc) {
        if (!StringUtils.hasText(tsc)) {
            throw new InvalidDataFormatException("TSC name cannot be empty or null");
        }
        if (tsc.length() > 50) {
            throw new InvalidDataFormatException("TSC name is too long (max 50 characters)");
        }
    }

    public void validateRawData(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidDataFormatException("Data cannot be null or empty");
        }

        int minSize = Math.min(
                properties.getDataHeader().getR25HeaderSize(),
                properties.getDataHeader().getR27HeaderSize()
        );

        if (data.length < minSize * 2) {
            throw new InvalidDataFormatException(
                    String.format("Data length (%d) is too short (minimum: %d)", data.length, minSize * 2)
            );
        }
    }

    public void validateDataField(byte[] dataField, int requiredMinLength) {
        if (dataField == null || dataField.length < requiredMinLength) {
            throw new InvalidDataFormatException(
                    String.format("Data field length (%d) is insufficient (required: %d)",
                            dataField != null ? dataField.length : 0, requiredMinLength)
            );
        }
    }
}