package com.xml.processor.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public ValidationException(List<String> validationErrors) {
        super(String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = List.of(message);
    }
} 