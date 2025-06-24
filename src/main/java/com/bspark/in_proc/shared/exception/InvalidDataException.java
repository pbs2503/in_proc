package com.bspark.in_proc.shared.exception;

public class InvalidDataException extends DataProcessingException {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}