package com.bspark.in_proc.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tsc.processing")
public class TscProcessingProperties {

    private DataHeader dataHeader = new DataHeader();
    private DataTail dataTail = new DataTail();
    private Standard standard = new Standard();
    private Logging logging = new Logging();

    @Data
    public static class DataHeader {
        private int r25HeaderSize = 5;
        private int r27HeaderSize = 8;
        private byte[] r25StartBytes = {0x7E, 0x7E};
        private byte[] r27StartBytes = {0x7F, 0x7F};
    }

    @Data
    public static class DataTail {
        private int r25TailSize = 1;
        private int r27TailSize = 2;
    }

    @Data
    public static class Standard {
        private int r25 = 25;
        private int r27 = 27;
        private int unknown = 0;
    }

    @Data
    public static class Logging {
        private boolean enablePrettyJson = true;
        private boolean enableDataLogging = false;
    }
}