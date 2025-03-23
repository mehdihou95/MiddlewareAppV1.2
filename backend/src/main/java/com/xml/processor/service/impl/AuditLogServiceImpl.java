package com.xml.processor.service.impl;

import com.xml.processor.model.AuditLog;
import com.xml.processor.repository.AuditLogRepository;
import com.xml.processor.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByClientId(Long clientId, Pageable pageable) {
        return auditLogRepository.findByClientId(clientId, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByUsernameAndDateRange(String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByUsernameAndCreatedAtBetween(username, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByClientIdAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByClientIdAndCreatedAtBetween(clientId, startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByResponseStatus(Integer status, Pageable pageable) {
        return auditLogRepository.findByResponseStatus(status, pageable);
    }

    @Override
    @Transactional
    public void deleteAuditLogsOlderThan(LocalDateTime date) {
        auditLogRepository.deleteByCreatedAtBefore(date);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void cleanupOldAuditLogs() {
        // Delete audit logs older than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        deleteAuditLogsOlderThan(thirtyDaysAgo);
    }
} 