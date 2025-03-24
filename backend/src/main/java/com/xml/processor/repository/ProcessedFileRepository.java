package com.xml.processor.repository;

import com.xml.processor.model.ProcessedFile;
import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ProcessedFile entities.
 */
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

    List<ProcessedFile> findByClient(Client client);
    
    List<ProcessedFile> findByInterfaceEntity(Interface interfaceEntity);
    
    Page<ProcessedFile> findByInterfaceEntity_Id(Long interfaceId, Pageable pageable);
    
    Page<ProcessedFile> findByStatusAndProcessedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ProcessedFile> findByInterfaceEntityId(Long interfaceId, Pageable pageable);

    @Query("SELECT p FROM ProcessedFile p WHERE " +
           "(:searchTerm IS NULL OR p.fileName LIKE %:searchTerm%) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.processedAt >= :startDate) AND " +
           "(:endDate IS NULL OR p.processedAt <= :endDate)")
    Page<ProcessedFile> findBySearchCriteria(
            @Param("searchTerm") String searchTerm,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
} 