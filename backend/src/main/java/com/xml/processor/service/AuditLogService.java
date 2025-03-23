package com.xml.processor.service;

import com.xml.processor.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {
    AuditLog createAuditLog(AuditLog auditLog);
    
    Page<AuditLog> getAuditLogs(Pageable pageable);
    
    Page<AuditLog> getAuditLogsByUsername(String username, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByClientId(Long clientId, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByUsernameAndDateRange(String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByClientIdAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<AuditLog> getAuditLogsByResponseStatus(Integer status, Pageable pageable);
    
    void deleteAuditLogsOlderThan(LocalDateTime date);
} 