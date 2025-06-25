
package com.bspark.in_proc.domain.service;

import com.bspark.in_proc.domain.model.TscData;
import com.bspark.in_proc.infrastructure.converter.standard.StandardDetector;
import com.bspark.in_proc.infrastructure.converter.tsc.builder.DetectorInfoJsonBuilder;
import com.bspark.in_proc.infrastructure.converter.tsc.builder.PhaseInfoJsonBuilder;
import com.bspark.in_proc.infrastructure.converter.tsc.parser.DetectorInfoParser;
import com.bspark.in_proc.infrastructure.converter.tsc.parser.PhaseInfoParser;
import com.bspark.in_proc.infrastructure.converter.tsc.parser.TscStatusParser;
import com.bspark.in_proc.infrastructure.converter.tsc.builder.IntersectionStatusJsonBuilder;
import com.bspark.in_proc.shared.constant.TscConstants;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.UnsupportedStandardException;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TscDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TscDataProcessor.class);

    private final StandardDetector standardDetector;
    private final TscStatusParser statusParser;
    private final DetectorInfoParser detectorInfoParser;
    private final PhaseInfoParser phaseInfoParser;
    private final IntersectionStatusJsonBuilder intersectionStatusJsonBuilder;
    private final DetectorInfoJsonBuilder detectorInfoJsonBuilder;
    private final DataValidationService validationService;
    private final TscProcessingProperties properties;
    private final PhaseInfoJsonBuilder phaseInfoJsonBuilder;

    @Autowired
    public TscDataProcessor(StandardDetector standardDetector,
                            TscStatusParser statusParser,
                            DetectorInfoParser detectorInfoParser,
                            PhaseInfoParser phaseInfoParser,
                            IntersectionStatusJsonBuilder intersectionStatusJsonBuilder,
                            DetectorInfoJsonBuilder detectorInfoJsonBuilder,
                            DataValidationService validationService,
                            TscProcessingProperties properties, PhaseInfoJsonBuilder phaseInfoJsonBuilder) {
        this.standardDetector = standardDetector;
        this.statusParser = statusParser;
        this.detectorInfoParser = detectorInfoParser;
        this.phaseInfoParser = phaseInfoParser;
        this.intersectionStatusJsonBuilder = intersectionStatusJsonBuilder;
        this.detectorInfoJsonBuilder = detectorInfoJsonBuilder;
        this.validationService = validationService;
        this.properties = properties;
        this.phaseInfoJsonBuilder = phaseInfoJsonBuilder;
    }

    public TscData process(String tscId, byte[] rawData, String messageType) {
        logger.info("=== TSC PROCESSING START === TSC: {}, Type: {}", tscId, messageType);

        try {
            // 입력 검증
            validationService.validateTscId(tscId);
            validationService.validateRawData(rawData);
            logger.debug("Input validation completed for TSC: {}", tscId);

            // 표준 검출
            int standard = standardDetector.detectStandard(rawData);
            if (standard == TscConstants.STANDARD_UNKNOWN) {
                throw new UnsupportedStandardException("Unable to detect data standard for TSC: " + tscId);
            }
            logger.info("Detected standard R{} for TSC: {}", standard, tscId);

            // 메시지 타입별 처리
            String jsonData = processMessageType(tscId, rawData, messageType, standard);

            // 도메인 모델 생성
            TscData tscData = TscData.builder()
                    .tscId(tscId)
                    .messageType(messageType)
                    .standard(standard)
                    .jsonData(jsonData)
                    .rawData(rawData)
                    .timestamp(LocalDateTime.now())
                    .dataLength(rawData.length)
                    .build();

            logger.info("=== TSC PROCESSING COMPLETE === TSC: {}, Type: {}", tscId, messageType);
            return tscData;

        } catch (Exception e) {
            logger.error("=== TSC PROCESSING FAILED === TSC: {}, Type: {}", tscId, messageType, e);
            throw new DataProcessingException("Failed to process TSC data for: " + tscId, e);
        }
    }

    private String processMessageType(String tscId, byte[] rawData, String messageType, int standard) {
        return switch (messageType) {
            case "INTERSECTION_STATUS" -> {
                var statusData = statusParser.parse(rawData, standard);
                JSONObject json = intersectionStatusJsonBuilder.buildJson(tscId, standard, statusData);
                logger.debug("INTERSECTION_STATUS processing completed for TSC: {}", tscId);
                yield json.toJSONString();
            }
            case "DETECTOR_INFO" -> {
                var detectorData = detectorInfoParser.parse(rawData, standard);
                JSONObject json = detectorInfoJsonBuilder.buildJson(tscId, standard, detectorData);
                logger.debug("DETECTOR_INFO processing completed for TSC: {}", tscId);
                yield json.toJSONString();
            }
            case "PHASE_INFO" -> {
                var phaseData = phaseInfoParser.parse(rawData, standard);
                JSONObject json = phaseInfoJsonBuilder.buildJson(tscId, standard, phaseData);
                logger.debug("PHASE_INFO processing completed for TSC: {}", tscId);
                yield json.toJSONString();
            }
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}