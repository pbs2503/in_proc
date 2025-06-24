package com.bspark.in_proc.infrastructure.converter;

import com.bspark.in_proc.infrastructure.converter.standard.StandardChecker;
import com.bspark.in_proc.shared.util.Utility;
import com.bspark.in_proc.shared.config.properties.TscProcessingProperties;
import com.bspark.in_proc.infrastructure.converter.tsc.ConvertTscStatus;
import com.bspark.in_proc.shared.exception.DataProcessingException;
import com.bspark.in_proc.shared.exception.UnsupportedStandardException;
import com.bspark.in_proc.domain.service.validator.DataValidator;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ByteToJsonConverter {

    private static final Logger logger = LoggerFactory.getLogger(ByteToJsonConverter.class);

    // 통계 정보를 위한 카운터
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    private final Byte2ObjectMap<Convert> convertMap = new Byte2ObjectOpenHashMap<>();
    private final Executor asyncExecutor;
    private final StandardChecker standardChecker;
    private final TscProcessingProperties properties;
    private final DataValidator dataValidator;

    @Autowired
    public ByteToJsonConverter(@Qualifier("asyncConverterExecutor") Executor asyncExecutor,
                               ConvertTscStatus convertTscStatus,
                               StandardChecker standardChecker,
                               TscProcessingProperties properties,
                               DataValidator dataValidator) {
        this.asyncExecutor = asyncExecutor;
        this.standardChecker = standardChecker;
        this.properties = properties;
        this.dataValidator = dataValidator;

        // 컨버터 매핑 초기화
        initializeConverterMap(convertTscStatus);
    }

    private void initializeConverterMap(ConvertTscStatus convertTscStatus) {
        // TSC Status 관련 OPCODE들
        convertMap.put((byte) 0x13, convertTscStatus);
        convertMap.put((byte) 0x23, convertTscStatus);
        convertMap.put((byte) 0x33, convertTscStatus);

        logger.info("Initialized converter map with {} converters", convertMap.size());
    }

    public CompletableFuture<Void> processAsync(String tsc, byte[] data) {
        return CompletableFuture.runAsync(() -> {
            Instant startTime = Instant.now();
            long currentProcessCount = processedCount.incrementAndGet();

            logger.debug("Starting async processing [TSC: {}] [Process #{}] [Data Length: {}]",
                    tsc, currentProcessCount, data.length);

            try {
                // 입력 검증
                dataValidator.validateTscName(tsc);
                dataValidator.validateRawData(data);

                // 표준 확인
                int standard = standardChecker.checkDataStandard(data);

                // 알 수 없는 표준 처리
                if (standard == properties.getStandard().getUnknown()) {
                    logger.warn("Unknown data standard detected, skipping processing [TSC: {}]", tsc);
                    return;
                }

                // OPCODE 추출
                byte opcode = extractOpcode(data, standard);

                // 컨버터 조회 및 실행
                Convert converter = convertMap.get(opcode);
                if (converter != null) {
                    converter.process(tsc, data, standard);
                    successCount.incrementAndGet();

                    Duration processingTime = Duration.between(startTime, Instant.now());
                    // 수정된 로그 메시지 - String.format 사용
                    logger.debug("Successfully processed [TSC: {}] [OPCODE: {}] [Standard: R{}] [Time: {}ms]",
                            tsc,
                            String.format("0x%02X", opcode & 0xFF),
                            standard,
                            processingTime.toMillis());
                } else {
                    handleUndefinedOpcode(tsc, data, standard, opcode);
                }

            } catch (DataProcessingException e) {
                errorCount.incrementAndGet();
                logger.error("Data processing failed [TSC: {}] [Process #{}]: {}",
                        tsc, currentProcessCount, e.getMessage(), e);
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.error("Unexpected error during async processing [TSC: {}] [Process #{}]",
                        tsc, currentProcessCount, e);
            }
        }, asyncExecutor);
    }

    private byte extractOpcode(byte[] data, int standard) {
        if (standard == properties.getStandard().getR25()) {
            if (data.length <= 4) {
                throw new DataProcessingException("R25 data is too short to extract opcode");
            }
            return data[4];
        } else if (standard == properties.getStandard().getR27()) {
            if (data.length <= 7) {
                throw new DataProcessingException("R27 data is too short to extract opcode");
            }
            return data[7];
        } else {
            throw new UnsupportedStandardException(standard);
        }
    }

    private void handleUndefinedOpcode(String tsc, byte[] data, int standard, byte opcode) {
        errorCount.incrementAndGet();

        // 수정된 로그 메시지 - String.format 사용
        logger.warn("Undefined OPCODE detected [TSC: {}] [OPCODE: {}] [Standard: R{}]",
                tsc, String.format("0x%02X", opcode & 0xFF), standard);

        if (properties.getLogging().isEnableDataLogging()) {
            logger.error("Raw data for undefined OPCODE [TSC: {}] -> {}", tsc, Utility.toHexString(data));
        }
    }

    // 통계 정보 조회 메서드들
    public long getProcessedCount() {
        return processedCount.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public double getSuccessRate() {
        long processed = processedCount.get();
        return processed > 0 ? (double) successCount.get() / processed * 100.0 : 0.0;
    }

    public void resetStatistics() {
        processedCount.set(0);
        successCount.set(0);
        errorCount.set(0);
        logger.info("Converter statistics have been reset");
    }
}