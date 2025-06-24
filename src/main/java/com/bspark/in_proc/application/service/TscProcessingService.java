package com.bspark.in_proc.application.service;

import com.bspark.in_proc.domain.model.DetectorInfoData;
import com.bspark.in_proc.domain.model.PhaseInfoData;
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

    private final TscDataProcessor dataProcessor;
    private final TscDataRepository dataRepository;

    @Autowired
    public TscProcessingService(TscDataProcessor dataProcessor,
                                TscDataRepository dataRepository) {
        this.dataProcessor = dataProcessor;
        this.dataRepository = dataRepository;
    }

    public CompletableFuture<ProcessingResult> processIntersectionStatusDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("========== TSC PROCESSING SERVICE START ========== TSC: {}", tscId);

            try {
                // 도메인 서비스를 통한 데이터 처리
                TscData tscData = dataProcessor.process(tscId, rawData, "INTERSECTION_STATUS");
                logger.info("Domain processing completed for TSC: {}", tscId);

                // 저장소에 저장
                dataRepository.save(tscData);
                logger.info("Repository save completed for TSC: {}", tscId);

                // 저장 확인
                boolean exists = dataRepository.exists(tscId);
                logger.info("Final verification - TSC {} exists in repository: {}", tscId, exists);

                logger.info("========== TSC PROCESSING SERVICE SUCCESS ========== TSC: {}", tscId);
                return ProcessingResult.success(tscData);

            } catch (Exception e) {
                logger.error("========== TSC PROCESSING SERVICE FAILED ========== TSC: {}", tscId, e);
                return ProcessingResult.failure(e.getMessage());
            }
        });
    }

    public CompletableFuture<TscData> processDetectorInfoDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TscData result = dataProcessor.process(tscId, rawData, "DETECTOR_INFO");
                dataRepository.save(result);
                logger.info("Successfully processed DETECTOR_INFO data for TSC: {}", tscId);
                return result;
            } catch (Exception e) {
                logger.error("Failed to process DETECTOR_INFO data for TSC: {}", tscId, e);
                throw new DataProcessingException("Failed to process detector info data", e);
            }
        });
    }

    public CompletableFuture<TscData> processPhaseInfoDataAsync(String tscId, byte[] rawData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TscData result = dataProcessor.process(tscId, rawData, "PHASE_INFO");
                dataRepository.save(result);
                logger.info("Successfully processed PHASE_INFO data for TSC: {}", tscId);
                return result;
            } catch (Exception e) {
                logger.error("Failed to process PHASE_INFO data for TSC: {}", tscId, e);
                throw new DataProcessingException("Failed to process phase info data", e);
            }
        });
    }
}