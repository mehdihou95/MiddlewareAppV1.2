package com.xml.processor.service.impl;

import com.xml.processor.model.ProcessedFile;
import com.xml.processor.repository.ProcessedFileRepository;
import com.xml.processor.service.ProcessedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessedFileServiceImpl implements ProcessedFileService {

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Override
    @Cacheable(value = "processedFiles", key = "#id")
    public ProcessedFile getProcessedFileById(Long id) {
        return processedFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcessedFile not found with id: " + id));
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'all'")
    public Page<ProcessedFile> getAllProcessedFiles(Pageable pageable) {
        return processedFileRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'client_' + #clientId")
    public Page<ProcessedFile> getProcessedFilesByClient(Long clientId, Pageable pageable) {
        return processedFileRepository.findByClientId(clientId, pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'search_' + #fileName")
    public Page<ProcessedFile> searchProcessedFiles(String fileName, Pageable pageable) {
        return processedFileRepository.findByFileNameContainingIgnoreCase(fileName, pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'status_' + #status")
    public Page<ProcessedFile> getProcessedFilesByStatus(String status, Pageable pageable) {
        return processedFileRepository.findByStatus(status, pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'date_range_' + #startDate + '_' + #endDate")
    public Page<ProcessedFile> getProcessedFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return processedFileRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'client_status_' + #clientId + '_' + #status")
    public Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, Pageable pageable) {
        return processedFileRepository.findByClientIdAndStatus(clientId, status, pageable);
    }

    @Override
    @Cacheable(value = "processedFiles", key = "'client_date_' + #clientId + '_' + #startDate + '_' + #endDate")
    public Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return processedFileRepository.findByClient_IdAndProcessedAtBetween(clientId, startDate, endDate, pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = "processedFiles", allEntries = true)
    public ProcessedFile createProcessedFile(ProcessedFile processedFile) {
        return processedFileRepository.save(processedFile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "processedFiles", key = "#id")
    public ProcessedFile updateProcessedFile(Long id, ProcessedFile processedFile) {
        ProcessedFile existingFile = getProcessedFileById(id);
        // Update fields
        existingFile.setFileName(processedFile.getFileName());
        existingFile.setStatus(processedFile.getStatus());
        existingFile.setErrorMessage(processedFile.getErrorMessage());
        existingFile.setProcessedAt(processedFile.getProcessedAt());
        return processedFileRepository.save(existingFile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "processedFiles", key = "#id")
    public void deleteProcessedFile(Long id) {
        processedFileRepository.deleteById(id);
    }

    @Override
    public Page<ProcessedFile> getProcessedFiles(int page, int size, String sortBy, String sortDirection, String searchTerm, String status, LocalDateTime startDate, LocalDateTime endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            return searchProcessedFiles(searchTerm, pageable);
        } else if (status != null && !status.isEmpty()) {
            return getProcessedFilesByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            return getProcessedFilesByDateRange(startDate, endDate, pageable);
        }

        return getAllProcessedFiles(pageable);
    }

    @Override
    public Page<ProcessedFile> getProcessedFilesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return getProcessedFilesByClient(clientId, PageRequest.of(page, size, sort));
    }

    @Override
    public Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return getProcessedFilesByClientAndStatus(clientId, status, PageRequest.of(page, size, sort));
    }

    @Override
    public Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return getProcessedFilesByClientAndDateRange(clientId, startDate, endDate, PageRequest.of(page, size, sort));
    }

    @Override
    public List<ProcessedFile> getProcessedFilesByStatus(String status) {
        return processedFileRepository.findByStatus(status);
    }
} 