package com.xml.processor.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String username;
    
    @Column(name = "client_id")
    private Long clientId;
    
    @Column(nullable = false)
    private String details;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "request_method")
    private String requestMethod;
    
    @Column(name = "request_url")
    private String requestUrl;
    
    @Column(name = "request_params")
    private String requestParams;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "execution_time")
    private Long executionTime;
} 