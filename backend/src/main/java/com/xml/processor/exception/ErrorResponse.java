package com.xml.processor.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private List<String> validationErrors;

    public ErrorResponse(int status, String message, String details) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, List<String> validationErrors) {
        this.status = status;
        this.message = message;
        this.validationErrors = validationErrors;
        this.timestamp = LocalDateTime.now();
    }
} 