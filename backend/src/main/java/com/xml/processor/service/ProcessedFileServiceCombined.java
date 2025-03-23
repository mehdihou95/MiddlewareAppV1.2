package com.xml.processor.service;

import com.xml.processor.model.ProcessedFile;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProcessedFileServiceCombined {
    // New interface methods
    @Cacheable(value = "processedFiles", key = "#id")
    ProcessedFile getProcessedFileById(Long id);

    @Cacheable(value = "processedFiles", key = "'all'")
    Page<ProcessedFile> getAllProcessedFiles(Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'client_' + #clientId")
    Page<ProcessedFile> getProcessedFilesByClient(Long clientId, Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'search_' + #fileName")
    Page<ProcessedFile> searchProcessedFiles(String fileName, Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'status_' + #status")
    Page<ProcessedFile> getProcessedFilesByStatus(String status, Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'date_range_' + #startDate + '_' + #endDate")
    Page<ProcessedFile> getProcessedFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'client_status_' + #clientId + '_' + #status")
    Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, Pageable pageable);

    @Cacheable(value = "processedFiles", key = "'client_date_' + #clientId + '_' + #startDate + '_' + #endDate")
    Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @CacheEvict(value = "processedFiles", allEntries = true)
    ProcessedFile createProcessedFile(ProcessedFile processedFile);

    @CacheEvict(value = "processedFiles", key = "#id")
    ProcessedFile updateProcessedFile(Long id, ProcessedFile processedFile);

    @CacheEvict(value = "processedFiles", key = "#id")
    void deleteProcessedFile(Long id);

    Page<ProcessedFile> getProcessedFiles(int page, int size, String sortBy, String sortDirection, String fileNameFilter, String statusFilter, LocalDateTime startDate, LocalDateTime endDate);
    Page<ProcessedFile> getProcessedFilesByClient(Long clientId, int page, int size, String sortBy, String sortDirection);
    Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, int page, int size, String sortBy, String sortDirection);
    Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy, String sortDirection);
    List<ProcessedFile> getProcessedFilesByStatus(String status);

    // Legacy interface methods
    Optional<ProcessedFile> getProcessedFileByIdOptional(Long id);
    List<ProcessedFile> getAllProcessedFilesLegacy();
    List<ProcessedFile> getProcessedFilesByClient_Id(Long clientId);
    Optional<ProcessedFile> findByFileNameAndClient_Id(String fileName, Long clientId);
    List<ProcessedFile> findByStatusLegacy(String status);
    List<ProcessedFile> findByClient_IdAndStatus(Long clientId, String status);
    List<ProcessedFile> findByClient_IdAndProcessingStartTimeBetween(Long clientId, String startTime, String endTime);
    List<ProcessedFile> findByClient_IdAndHasErrors(Long clientId);
    List<ProcessedFile> findLatestProcessedFiles(Long clientId);
} 