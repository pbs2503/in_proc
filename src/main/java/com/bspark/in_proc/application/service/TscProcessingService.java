package com.bspark.in_proc.application.service;

import com.bspark.in_proc.domain.model.TscData;
import com.bspark.in_proc.domain.model.ProcessingResult;
import com.bspark.in_proc.domain.service.TscDataProcessor;
import com.bspark.in_proc.domain.repository.TscDataRepository;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TscProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(TscProcessingService.class);

    private final TscDataProcessor tscDataProcessor;
    private final TscDataRepository dataRepository;
    private final DetectorUnitStatusService detectorStatusService;
    private final PhaseInfoService phaseInfoService;

    @Autowired
    public TscProcessingService(TscDataProcessor tscDataProcessor,
                                TscDataRepository dataRepository,
                                DetectorUnitStatusService detectorStatusService, PhaseInfoService phaseInfoService) {
        this.tscDataProcessor = tscDataProcessor;
        this.dataRepository = dataRepository;
        this.detectorStatusService = detectorStatusService;
        this.phaseInfoService = phaseInfoService;
    }

    public CompletableFuture<ProcessingResult> processIntersectionStatusDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("========== TSC PROCESSING SERVICE START ========== TSC: {}", tscId);

            try {
                // 도메인 서비스를 통한 데이터 처리
                TscData tscData = tscDataProcessor.process(tscId, rawData, "INTERSECTION_STATUS");
                logger.debug("Domain processing completed for TSC: {}", tscId);

                // Redis에 저장
                dataRepository.save(tscData);
                logger.debug("Repository save completed for TSC: {}", tscId);

                // 저장 확인
                boolean exists = dataRepository.exists(tscId);
                logger.debug("Final verification - TSC {} exists in repository: {}", tscId, exists);

                logger.info("Successfully processed INTERSECTION_STATUS data for TSC: {}", tscId);
                return ProcessingResult.success(tscData);

            } catch (Exception e) {
                logger.error("========== TSC PROCESSING SERVICE FAILED ========== TSC: {}", tscId, e);
                return ProcessingResult.failure(e.getMessage());
            }
        });
    }

    public CompletableFuture<ProcessingResult> processDetectorInfoDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("========== DETECTOR INFO PROCESSING START ========== TSC: {}", tscId);

            try {
                // 도메인 서비스를 통한 데이터 처리
                TscData detData = tscDataProcessor.process(tscId, rawData, "DETECTOR_INFO");
                logger.debug("Domain processing completed for TSC: {}", tscId);

                // PostgreSQL에 저장
                detectorStatusService.saveDetectorStatus(detData);
                logger.debug("PostgreSQL save completed for TSC: {}", tscId);

                logger.info("Successfully processed DETECTOR_INFO data for TSC: {}", tscId);
                return ProcessingResult.success(detData);

            } catch (Exception e) {
                logger.error("========== DETECTOR INFO PROCESSING FAILED ========== TSC: {}", tscId, e);
                return ProcessingResult.failure(e.getMessage());
            }
        });
    }

    public CompletableFuture<ProcessingResult> processPhaseInfoDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 도메인 서비스를 통한 데이터 처리
                TscData phaseData = tscDataProcessor.process(tscId, rawData, "PHASE_INFO");
                logger.debug("Domain processing completed for TSC: {}", tscId);

                phaseInfoService.savePhaseInfo(phaseData);
                logger.debug("PostgreSQL save completed for TSC: {}", tscId);

                logger.info("Successfully processed PHASE_INFO data for TSC: {}", tscId);
                return ProcessingResult.success(phaseData);

            } catch (Exception e) {
                logger.error("Failed to process PHASE_INFO data for TSC: {}", tscId, e);
                return ProcessingResult.failure(e.getMessage());
            }
        });
    }
}