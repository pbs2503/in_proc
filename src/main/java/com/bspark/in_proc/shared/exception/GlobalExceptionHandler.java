package com.bspark.in_proc.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidDataFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDataFormat(InvalidDataFormatException e) {
        logger.error("Invalid data format error: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = Map.of(
                "error", "INVALID_DATA_FORMAT",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(UnsupportedStandardException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedStandard(UnsupportedStandardException e) {
        logger.error("Unsupported standard error: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = Map.of(
                "error", "UNSUPPORTED_STANDARD",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleDataProcessing(DataProcessingException e) {
        logger.error("Data processing error: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = Map.of(
                "error", "DATA_PROCESSING_ERROR",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Illegal argument error: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = Map.of(
                "error", "ILLEGAL_ARGUMENT",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", "An unexpected error occurred",
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}