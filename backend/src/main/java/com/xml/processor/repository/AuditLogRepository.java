package com.xml.processor.repository;

import com.xml.processor.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    Page<AuditLog> findByClientId(Long clientId, Pageable pageable);
    Page<AuditLog> findByAction(String action, Pageable pageable);
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<AuditLog> findByUsernameAndCreatedAtBetween(String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<AuditLog> findByClientIdAndCreatedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<AuditLog> findByResponseStatus(Integer status, Pageable pageable);
    void deleteByCreatedAtBefore(LocalDateTime date);
} 