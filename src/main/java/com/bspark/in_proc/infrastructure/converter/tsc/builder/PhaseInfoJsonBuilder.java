package com.bspark.in_proc.infrastructure.converter.tsc.builder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bspark.in_proc.domain.model.PhaseInfoData;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.phase.PhaseDataLine;
import com.bspark.in_proc.infrastructure.converter.tsc.wrapper.phase.PhaseDataWrapper;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PhaseInfoJsonBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PhaseInfoJsonBuilder.class);

    private final ConcurrentHashMap<String, JSONObject> templateCache = new ConcurrentHashMap<>();
    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public PhaseInfoJsonBuilder(TscProcessingProperties properties, DataValidator dataValidator) {
        this.properties = properties;
        this.dataValidator = dataValidator;
    }

    public JSONObject buildJson(String tsc, int standard, PhaseInfoData originData) {
        logger.debug("Building Detector Info JSON for TSC: {} with standard R{}", tsc, standard);

        try {
            dataValidator.validateTscName(tsc);
            dataValidator.validateDataField(originData.getDataField(), 22);

            // 1. 템플릿 로딩 or 생성
            JSONObject template = templateCache.computeIfAbsent(tsc, key -> buildEmptyJsonTemplate());

            // 2. 템플릿 복사
            JSONObject json = JSON.parseObject(JSON.toJSONString(template));

            // 3. 실 데이터 채움
            fillPhaseInfoData(json, originData.getDataField());

            logger.debug("Successfully built Detector Info JSON for TSC: {}", tsc);
            return json;

        } catch (Exception e) {
            logger.error("Failed to build TSC Status JSON for TSC: {}", tsc, e);
            throw new DataProcessingException("Failed to build TSC Status JSON for TSC: " + tsc, e);
        }
    }

    private JSONObject buildEmptyJsonTemplate() {
        List<Integer> defaultList = Arrays.asList(0,0,1,1,2,2,3,3);
        List<Integer> defaultList2 = Arrays.asList(9,9,8,8,7,7,6,6);

        PhaseDataLine line = new PhaseDataLine(defaultList, defaultList2);
        PhaseDataWrapper wrapper = new PhaseDataWrapper(line, line);

        return JSON.parseObject(JSON.toJSONString(wrapper));
    }

    private void fillPhaseInfoData(JSONObject json, byte[] dataField) {
        JSONObject pedestrian = json.getJSONObject("pedestrian");
        JSONObject vehicle = json.getJSONObject("vehicle");

        vehicle.put("A", extractPhaseData(dataField, 0));
        vehicle.put("B", extractPhaseData(dataField, 8));
        pedestrian.put("A", extractPhaseData(dataField, 16));
        pedestrian.put("B", extractPhaseData(dataField, 24));
    }

    private List<Integer> extractPhaseData(byte[] dataField, int startIdx) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            result.add(dataField[startIdx + i] & 0xFF);
        }
        return result;
    }


}
