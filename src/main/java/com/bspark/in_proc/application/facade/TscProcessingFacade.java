package com.bspark.in_proc.application.facade;

import com.bspark.in_proc.application.service.TscProcessingService;
import com.bspark.in_proc.domain.repository.TscDataRepository;
import com.bspark.in_proc.interfaces.dto.request.TscDataRequest;
import com.bspark.in_proc.interfaces.dto.response.TscStatusResponse;
import com.bspark.in_proc.shared.util.HexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TscProcessingFacade {

    private static final Logger logger = LoggerFactory.getLogger(TscProcessingFacade.class);

    private final TscProcessingService processingService;
    private final TscDataRepository dataRepository;

    @Autowired
    public TscProcessingFacade(TscProcessingService processingService,
                               TscDataRepository dataRepository) {
        this.processingService = processingService;
        this.dataRepository = dataRepository;
    }
/*
    public CompletableFuture<Void> processAsync(String tscId, byte[] rawData) {
        return processingService.processAsync(tscId, rawData).thenAccept(result -> {
            // 처리 완료 후 추가 작업
        });
    }
*/
public void processAsync(String tscId, TscDataRequest request) {
    var messageType = request.getType();
    var rawData = HexConverter.hexStringToByteArray(request.getData());

    switch (messageType) {
        case "INTERSECTION_STATUS" -> processingService.processIntersectionStatusDataAsync(tscId, rawData)
                .thenAccept(result -> logger.info("Completed INTERSECTION_STATUS processing for TSC: {}", tscId));

        case "DETECTOR_INFO" -> processingService.processDetectorInfoDataAsync(tscId, rawData)
                .thenAccept(result -> logger.info("Completed DETECTOR_INFO processing for TSC: {}", tscId));

        case "PHASE_INFO" -> processingService.processPhaseInfoDataAsync(tscId, rawData)
                .thenAccept(result -> logger.info("Completed PHASE_INFO processing for TSC: {}", tscId));

        default -> throw new IllegalStateException("Unexpected message type: " + messageType);
    }
}

    public TscStatusResponse getStatus(String tscId) {
        return dataRepository.findById(tscId)
                .map(tscData -> TscStatusResponse.builder()
                        .success(true)
                        .tscId(tscId)
                        .data(tscData.getJsonData())
                        .build())
                .orElse(TscStatusResponse.builder()
                        .success(false)
                        .tscId(tscId)
                        .message("TSC data not found")
                        .build());
    }
}