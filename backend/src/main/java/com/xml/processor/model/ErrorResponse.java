package com.xml.processor.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private List<String> details;
    private Map<String, String> fieldErrors;
    
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new HashMap<>();
    }

    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.fieldErrors = new HashMap<>();
    }

    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp, List<String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
        this.fieldErrors = new HashMap<>();
    }

    public ErrorResponse(String code, String message) {
        this.status = 400;
        this.error = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new HashMap<>();
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public void addFieldError(String field, String message) {
        this.fieldErrors.put(field, message);
    }
} 