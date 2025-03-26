package com.xml.processor.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends ApplicationException {
    private final Map<String, String> fieldErrors;
    
    /**
     * Constructs a new ValidationException with the specified message.
     *
     * @param message The error message
     */
    public ValidationException(String message) {
        super(ErrorCodes.VAL_INVALID_FORMAT, message);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Constructs a new ValidationException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ValidationException(String message, Throwable cause) {
        super(ErrorCodes.VAL_INVALID_FORMAT, message, cause);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(ErrorCodes.VAL_INVALID_FORMAT, message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
} 