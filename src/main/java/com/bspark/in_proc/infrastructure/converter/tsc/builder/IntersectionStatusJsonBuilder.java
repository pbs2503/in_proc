package com.bspark.in_proc.infrastructure.converter.tsc.builder;

import com.alibaba.fastjson2.JSONObject;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.domain.model.TscStatusData;
import com.bspark.in_proc.shared.constant.TscStatusConstants;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class IntersectionStatusJsonBuilder {

    private static final Logger logger = LoggerFactory.getLogger(IntersectionStatusJsonBuilder.class);

    // 성능 최적화를 위한 캐시
    private final ConcurrentHashMap<String, JSONObject> templateCache = new ConcurrentHashMap<>();

    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public IntersectionStatusJsonBuilder(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public JSONObject buildJson(String tsc, int standard, TscStatusData statusData) {
        logger.debug("Building Intersection Status JSON for TSC: {} with standard R{}", tsc, standard);

        try {
            // 기본 검증
            dataValidator.validateTscName(tsc);
            dataValidator.validateDataField(statusData.getDataField(), 23);

            JSONObject json = createBaseJson(tsc, standard);
            byte[] dataField = statusData.getDataField();

            // 각 섹션 구성 - 원하는 형태의 JSON 구조
            json.put("status", buildStatusSection(dataField));
            json.put("operation", buildOperationSection(dataField));
            json.put("control", buildControlSection(dataField));
            json.put("panel", buildPanelSection(dataField));

            logger.debug("Successfully built Intersection Status JSON for TSC: {}", tsc);
            return json;

        } catch (Exception e) {
            logger.error("Failed to build Intersection Status JSON for TSC: {}", tsc, e);
            throw new DataProcessingException("Failed to build Intersection Status JSON for TSC: " + tsc, e);
        }
    }

    private JSONObject createBaseJson(String tsc, int standard) {
        JSONObject json = new JSONObject();
        json.put("TSC-NAME", tsc);  // 원하는 형태
        json.put("STANDARD", String.format("R%d", standard));  // 원하는 형태
        return json;
    }

    private JSONObject buildStatusSection(byte[] dataField) {
        JSONObject status = new JSONObject();

        // 안전한 배열 접근을 위한 헬퍼 메서드 사용
        status.put("power-fail", TscStatusConstants.getStatus(getByteValue(dataField, 0), 0x80));
        status.put("mcu-scu", TscStatusConstants.getStatus(getByteValue(dataField, 0), 0x40));
        status.put("flash-status", TscStatusConstants.getStatus(getByteValue(dataField, 3), 0x02));
        status.put("light-off-status", TscStatusConstants.getStatus(getByteValue(dataField, 3), 0x04));
        status.put("conflict-status", TscStatusConstants.getStatus(getByteValue(dataField, 3), 0x08));
        status.put("detect-conflict", TscStatusConstants.getEnabledStatus(getByteValue(dataField, 4), 0x02));
        status.put("pedestrian-button", TscStatusConstants.getEnabledStatus(getByteValue(dataField, 4), 0x80));

        return status;
    }

    private JSONObject buildOperationSection(byte[] dataField) {
        JSONObject operation = new JSONObject();

        // 주기 정보
        operation.put("cycle", String.format("%d / %d",
                getByteValue(dataField, 10), getByteValue(dataField, 12)));

        // 운영 모드 및 타입
        operation.put("light-type", (getByteValue(dataField, 16) & 0x80) == 0 ? "3Color" : "4Color");
        operation.put("ring-type", (getByteValue(dataField, 0) & 0x10) == 0 ? "SINGLE" : "DUAL");
        operation.put("operation-mode", TscStatusConstants.getOperationMode(getByteValue(dataField, 0)));
        operation.put("operation-map", TscStatusConstants.getOperationMap(getByteValue(dataField, 16)));

        // 현시 및 스텝 정보
        int phase1 = (getByteValue(dataField, 1) >> 4) + 1;
        int phase2 = (getByteValue(dataField, 2) >> 4) + 1;
        int step1 = getByteValue(dataField, 1) & 0b1111;
        int step2 = getByteValue(dataField, 2) & 0b1111;

        operation.put("phase", String.format("%d / %d", phase1, phase2));
        operation.put("step", String.format("%d / %d", step1, step2));

        // 기타 운영 정보
        operation.put("prev-cycle", getByteValue(dataField, 11));
        operation.put("offset", getByteValue(dataField, 13));
        operation.put("phase-hold", formatOptionalValue(getByteValue(dataField, 14)));
        operation.put("phase-omit", formatOptionalValue(getByteValue(dataField, 15)));

        return operation;
    }

    private JSONObject buildControlSection(byte[] dataField) {
        JSONObject control = new JSONObject();
        control.put("ppc", createPpcControl(dataField));  // "ppc-control" → "ppc"
        control.put("spillback", createSpillbackControl(dataField));  // "spillback-control" → "spillback"
        return control;
    }

    private JSONObject createPpcControl(byte[] dataField) {
        JSONObject ppcObj = new JSONObject();
        int ppcByte = getByteValue(dataField, 22);

        ppcObj.put("enable", TscStatusConstants.getOnOff(getByteValue(dataField, 0), 0x08));
        ppcObj.put("in-service", (ppcByte & 0x80) == 0 ? "STAND-BY" : "IN-SERVICE");
        ppcObj.put("ppc-type", (ppcByte & 0x40) == 0 ? "EMERGENCY" : "BUS");
        ppcObj.put("ring-a", createRingPPC(ppcByte, 0x20, 0x10, 0x08));
        ppcObj.put("ring-b", createRingPPC(ppcByte, 0x04, 0x02, 0x01));

        return ppcObj;
    }

    private JSONObject createRingPPC(int value, int holdMask, int offMask, int jumpMask) {
        JSONObject ring = new JSONObject();
        ring.put("hold", TscStatusConstants.getOnOff(value, holdMask));
        ring.put("off", TscStatusConstants.getOnOff(value, offMask));
        ring.put("jump", TscStatusConstants.getOnOff(value, jumpMask));
        return ring;
    }

    private JSONObject createSpillbackControl(byte[] dataField) {
        JSONObject spillbackObj = new JSONObject();
        int spillbackByte = getByteValue(dataField, 16);

        spillbackObj.put("enable", TscStatusConstants.getOnOff(spillbackByte, 0x08));
        spillbackObj.put("mg-control", (spillbackByte & 0x04) == 0 ? "IGNORE" : "KEEP");
        spillbackObj.put("major-termination", (spillbackByte & 0x02) == 0 ? "EXEC" : "NONE");
        spillbackObj.put("minor-termination", (spillbackByte & 0x01) == 0 ? "EXEC" : "NONE");

        return spillbackObj;
    }

    private JSONObject buildPanelSection(byte[] dataField) {
        JSONObject panel = new JSONObject();

        panel.put("enable", (getByteValue(dataField, 4) & 0x04) == 0 ? "ENABLED" : "DISABLED");
        panel.put("manual-next", TscStatusConstants.getOnOff(getByteValue(dataField, 3), 0x80));
        panel.put("manual-switch", TscStatusConstants.getOnOff(getByteValue(dataField, 3), 0x40));
        panel.put("manual-flash", TscStatusConstants.getOnOff(getByteValue(dataField, 3), 0x20));
        panel.put("manual-lights-off", TscStatusConstants.getOnOff(getByteValue(dataField, 3), 0x10));

        return panel;
    }

    // 안전한 배열 접근을 위한 헬퍼 메서드
    private int getByteValue(byte[] dataField, int index) {
        if (index >= dataField.length) {
            logger.warn("Array index {} is out of bounds for data field length {}", index, dataField.length);
            return 0;
        }
        return dataField[index] & 0xFF;
    }

    // 옵셔널 값 포맷팅
    private String formatOptionalValue(int value) {
        return value == 0 ? "OFF" : String.valueOf(value);
    }
}