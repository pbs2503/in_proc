package com.bspark.in_proc.domain.service;

import com.bspark.in_proc.shared.constant.TscConstants;
import com.bspark.in_proc.shared.exception.InvalidDataFormatException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DataValidationService {

    public void validateTscId(String tscId) {
        if (!StringUtils.hasText(tscId)) {
            throw new InvalidDataFormatException("TSC ID cannot be empty");
        }

        if (tscId.length() > TscConstants.MAX_TSC_NAME_LENGTH) {
            throw new InvalidDataFormatException("TSC ID is too long: " + tscId.length());
        }

        // TSC ID 패턴 검증 (IP 주소 형태)
        if (!isValidTscId(tscId)) {
            throw new InvalidDataFormatException("Invalid TSC ID format: " + tscId);
        }
    }

    public void validateRawData(byte[] rawData) {
        if (rawData == null) {
            throw new InvalidDataFormatException("Raw data cannot be null");
        }

        if (rawData.length < TscConstants.MIN_DATA_LENGTH) {
            throw new InvalidDataFormatException("Raw data is too short: " + rawData.length);
        }
    }

    private boolean isValidTscId(String tscId) {
        // 간단한 IP 주소 패턴 검증
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return tscId.matches(ipPattern);
    }
}