package com.bspark.in_proc.infrastructure.converter.tsc.builder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bspark.in_proc.domain.model.DetectorInfoData;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector.DetectorInfoWrapper;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector.DetectorUnit;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector.DetectorUnitStatus;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.detector.DetectorUnitData;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DetectorInfoJsonBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DetectorInfoJsonBuilder.class);

    private final ConcurrentHashMap<String, JSONObject> templateCache = new ConcurrentHashMap<>();
    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public DetectorInfoJsonBuilder(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public JSONObject buildJson(String tsc, int standard, DetectorInfoData originData) {
        logger.debug("Building Detector Info JSON for TSC: {} with standard R{}", tsc, standard);

        try {
            dataValidator.validateTscName(tsc);
            dataValidator.validateDataField(originData.getDataField(), 224);

            // 1. 템플릿 로딩 or 생성
            JSONObject template = templateCache.computeIfAbsent(tsc, key -> buildEmptyJsonTemplate());

            // 2. 템플릿 복사
            JSONObject json = JSON.parseObject(JSON.toJSONString(template));

            // 3. 실 데이터 채움
            fillDetectorData(json, originData.getDataField());

            logger.debug("Successfully built Detector Info JSON for TSC: {}", tsc);
            return json;

        } catch (Exception e) {
            logger.error("Failed to build TSC Status JSON for TSC: {}", tsc, e);
            throw new DataProcessingException("Failed to build TSC Status JSON for TSC: " + tsc, e);
        }
    }

    private JSONObject buildEmptyJsonTemplate() {
        List<DetectorUnit> list = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            list.add(new DetectorUnit(
                    i,
                    new DetectorUnitStatus(0, 0, 0, 0, 0, 0, 0),
                    new DetectorUnitData(0, 0, 0, 0, 0, 0)
            ));
        }
        DetectorInfoWrapper wrapper = new DetectorInfoWrapper(list);
        return JSON.parseObject(JSON.toJSONString(wrapper));
    }

    private void fillDetectorData(JSONObject json, byte[] dataField) {
        JSONArray detectors = json.getJSONArray("detectors");
        for (int idx = 0; idx < 32; idx++) {
            JSONObject detector = detectors.getJSONObject(idx);

            JSONObject status = detector.getJSONObject("detectorUnitStatus");
            status.put("volume_error", getBit(dataField, idx, 0x40));
            status.put("occupy_error", getBit(dataField, idx, 0x20));
            status.put("non_occupy_error", getBit(dataField, idx, 0x10));
            status.put("oscillation", getBit(dataField, idx, 0x08));
            status.put("shortField", getBit(dataField, idx, 0x04));
            status.put("openField", getBit(dataField, idx, 0x02));
            status.put("installed", getBit(dataField, idx, 0x01));

            JSONObject data = detector.getJSONObject("detectorUnitData");
            data.put("non_occupy_time", getByteValue(dataField, idx + 32));
            data.put("occupy_time", getByteValue(dataField, idx + 64));
            data.put("volume", getByteValue(dataField, idx + 96));
            data.put("saturation", getByteValue(dataField, idx + 128));
            data.put("saturation_velocity", getByteValue(dataField, idx + 160));
            data.put("saturation_non_occupy_time", getByteValue(dataField, idx + 192));
        }
    }

    private int getBit(byte[] data, int index, int mask) {
        return ((getByteValue(data, index) & mask) != 0) ? 1 : 0;
    }

    private int getByteValue(byte[] dataField, int index) {
        if (index >= dataField.length) {
            logger.warn("Array index {} is out of bounds for data field length {}", index, dataField.length);
            return 0;
        }
        return dataField[index] & 0xFF;
    }
}

