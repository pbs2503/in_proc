package com.bspark.in_proc.application.service;

import com.bspark.in_proc.domain.model.TscData;
import com.bspark.in_proc.domain.repository.DetectorUnitStatusRepository;
import com.bspark.in_proc.domain.repository.PhaseInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PhaseInfoService {
    private static final Logger logger = LoggerFactory.getLogger(PhaseInfoService.class);

    private final PhaseInfoRepository phaseInfoRepository;

    @Autowired
    public PhaseInfoService(PhaseInfoRepository phaseInfoRepository) {
        this.phaseInfoRepository = phaseInfoRepository;
    }

    @Transactional
    public void savePhaseInfo(TscData tscData) {
        try {
            logger.debug("Saving detector status to PostgreSQL for TSC: {}", tscData.getTscId());

            phaseInfoRepository.insertPhaseInfo(
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
}
