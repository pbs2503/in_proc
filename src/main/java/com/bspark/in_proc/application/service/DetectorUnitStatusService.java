package com.bspark.in_proc.application.service;

import com.bspark.in_proc.domain.model.TscData;
import com.bspark.in_proc.domain.repository.DetectorUnitStatusRepository;
import com.bspark.in_proc.infrastructure.persistence.jpa.entity.DetectorUnitStatusTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DetectorUnitStatusService {
    private static final Logger logger = LoggerFactory.getLogger(DetectorUnitStatusService.class);

    private final DetectorUnitStatusRepository detectorStatusRepository;

    @Autowired
    public DetectorUnitStatusService(DetectorUnitStatusRepository detectorStatusRepository) {
        this.detectorStatusRepository = detectorStatusRepository;
    }

    @Transactional
    public void saveDetectorStatus(TscData tscData) {
        try {
            logger.debug("Saving detector status to PostgreSQL for TSC: {}", tscData.getTscId());

            detectorStatusRepository.insertDetectorStatus(
                    tscData.getTscId(),           // ip_address
                    tscData.getRawData(),         // raw_data
                    tscData.getJsonData(),        // detail_data
                    LocalDateTime.now()           // update_dt
            );

            logger.debug("Successfully saved detector status for TSC: {}", tscData.getTscId());

        } catch (Exception e) {
            logger.error("Failed to save detector status for TSC: {}", tscData.getTscId(), e);
            throw e;
        }
    }

    @Transactional
    public void saveDetectorStatusEntity(TscData tscData) {
        try {
            DetectorUnitStatusTable entity = DetectorUnitStatusTable.builder()
                    .ipAddress(tscData.getTscId())
                    .rawData(tscData.getRawData())
                    .detailData(tscData.getJsonData())
                    .updateDt(LocalDateTime.now())
                    .build();

            detectorStatusRepository.save(entity);
            logger.debug("Successfully saved detector status entity for TSC: {}", tscData.getTscId());

        } catch (Exception e) {
            logger.error("Failed to save detector status entity for TSC: {}", tscData.getTscId(), e);
            throw e;
        }
    }

}
