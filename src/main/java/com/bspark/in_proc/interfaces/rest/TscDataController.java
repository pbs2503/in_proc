package com.bspark.in_proc.interfaces.rest;

import com.bspark.in_proc.application.facade.TscProcessingFacade;
import com.bspark.in_proc.interfaces.dto.request.TscDataRequest;
import com.bspark.in_proc.interfaces.dto.response.TscStatusResponse;
import com.bspark.in_proc.shared.util.HexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TscDataController {

    private static final Logger logger = LoggerFactory.getLogger(TscDataController.class);

    private final TscProcessingFacade processingFacade;

    @Autowired
    public TscDataController(TscProcessingFacade processingFacade) {
        this.processingFacade = processingFacade;
    }

    @PostMapping("/tsc-data")
    public ResponseEntity<TscStatusResponse> processTscData(
            @RequestHeader("X-TSC-IP") String tscId,
            @Valid @RequestBody TscDataRequest request) {

        logger.info("Received TSC data from: {} with type: {}", tscId, request.getType());

        try {
            byte[] rawData = HexConverter.hexStringToByteArray(request.getData());

            // 타입에 따른 비동기 처리 시작
            processingFacade.processAsync(tscId, request);

            return ResponseEntity.ok(TscStatusResponse.builder()
                    .success(true)
                    .message("Data processing started for type: " + request.getType())
                    .tscId(tscId)
                    .build());

        } catch (Exception e) {
            logger.error("Failed to process TSC data from: {} with type: {}", tscId, request.getType(), e);

            return ResponseEntity.badRequest()
                    .body(TscStatusResponse.builder()
                            .success(false)
                            .message("Failed to process data: " + e.getMessage())
                            .tscId(tscId)
                            .build());
        }
    }

    @GetMapping("/status/{tscId}")
    public ResponseEntity<TscStatusResponse> getTscStatus(@PathVariable String tscId) {
        try {
            TscStatusResponse response = processingFacade.getStatus(tscId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get TSC status for: {}", tscId, e);
            return ResponseEntity.notFound().build();
        }
    }
}