package com.xml.processor.repository;

import com.xml.processor.model.ProcessedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Long> {
    /**
     * Find all processed files for a specific client
     *
     * @param clientId The ID of the client
     * @return List of processed files belonging to the client
     */
    List<ProcessedFile> findByClient_Id(Long clientId);

    /**
     * Find processed files by client ID and interface ID
     */
    @Query("SELECT p FROM ProcessedFile p WHERE p.client.id = ?1 AND p.interfaceEntity.id = ?2")
    List<ProcessedFile> findByClient_IdAndInterfaceEntity_Id(Long clientId, Long interfaceId);

    /**
     * Find processed files by client ID and status
     */
    @Query("SELECT p FROM ProcessedFile p WHERE p.client.id = ?1 AND p.status = ?2")
    List<ProcessedFile> findByClient_IdAndStatus(Long clientId, String status);

    /**
     * Find processed files by client ID and filename
     */
    List<ProcessedFile> findByClient_IdAndFileName(Long clientId, String fileName);

    List<ProcessedFile> findByStatus(String status);

    Page<ProcessedFile> findByClientId(Long clientId, Pageable pageable);
    Page<ProcessedFile> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);
    Page<ProcessedFile> findByStatus(String status, Pageable pageable);
    Page<ProcessedFile> findByProcessedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<ProcessedFile> findByClientIdAndStatus(Long clientId, String status, Pageable pageable);
    Page<ProcessedFile> findByClientIdAndProcessedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<ProcessedFile> findByClient_Id(Long clientId, Pageable pageable);
    Page<ProcessedFile> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<ProcessedFile> findByClient_IdAndStatus(Long clientId, String status, Pageable pageable);

    // Legacy interface methods
    List<ProcessedFile> findByClient_IdAndProcessedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate);
    List<ProcessedFile> findByClient_IdAndErrorMessageIsNotNull(Long clientId);
    List<ProcessedFile> findTop10ByClient_IdOrderByProcessedAtDesc(Long clientId);

    // Pageable methods
    Page<ProcessedFile> findByClient_IdAndProcessedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
} 