package com.bspark.in_proc.shared.exception;

public class UnsupportedStandardException extends DataProcessingException {

    public UnsupportedStandardException(String message) {
        super(message);
    }

    public UnsupportedStandardException(int standard) {
        super("Unsupported data standard: " + standard);
    }
}