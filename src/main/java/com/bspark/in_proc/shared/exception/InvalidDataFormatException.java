package com.bspark.in_proc.shared.exception;

public class InvalidDataFormatException extends DataProcessingException {

    public InvalidDataFormatException(String message) {
        super(message);
    }

    public InvalidDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}