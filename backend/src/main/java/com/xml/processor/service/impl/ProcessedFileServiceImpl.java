package com.xml.processor.service.impl;

import com.xml.processor.exception.ResourceNotFoundException;
import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.repository.ProcessedFileRepository;
import com.xml.processor.service.interfaces.ProcessedFileService;
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
import java.util.Optional;

/**
 * Implementation of ProcessedFileService.
 * Provides operations for managing processed files.
 */
@Service
public class ProcessedFileServiceImpl implements ProcessedFileService {

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getAllProcessedFiles(Pageable pageable) {
        return processedFileRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClient(Long clientId, Pageable pageable) {
        return processedFileRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByInterface(Long interfaceId, Pageable pageable) {
        return processedFileRepository.findByInterfaceEntity_Id(interfaceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return processedFileRepository.findByProcessedAtBetween(startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByStatus(String status, Pageable pageable) {
        return processedFileRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessedFile> getProcessedFileById(Long id) {
        return processedFileRepository.findById(id);
    }

    @Override
    @Transactional
    public ProcessedFile createProcessedFile(ProcessedFile processedFile) {
        validateProcessedFile(processedFile);
        processedFile.setProcessedAt(LocalDateTime.now());
        return processedFileRepository.save(processedFile);
    }

    @Override
    @Transactional
    public ProcessedFile updateProcessedFile(Long id, ProcessedFile processedFile) {
        ProcessedFile existingFile = processedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProcessedFile not found with id: " + id));
        
        validateProcessedFile(processedFile);
        
        existingFile.setFileName(processedFile.getFileName());
        existingFile.setStatus(processedFile.getStatus());
        existingFile.setErrorMessage(processedFile.getErrorMessage());
        existingFile.setInterfaceEntity(processedFile.getInterfaceEntity());
        existingFile.setClient(processedFile.getClient());
        existingFile.setProcessedAt(processedFile.getProcessedAt());
        
        return processedFileRepository.save(existingFile);
    }

    @Override
    @Transactional
    public void deleteProcessedFile(Long id) {
        if (!processedFileRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProcessedFile not found with id: " + id);
        }
        processedFileRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getErrorFiles(Pageable pageable) {
        return processedFileRepository.findByStatus("ERROR", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getErrorFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return processedFileRepository.findByStatusAndProcessedAtBetween("ERROR", startDate, endDate, pageable);
    }

    private void validateProcessedFile(ProcessedFile processedFile) {
        if (processedFile.getFileName() == null || processedFile.getFileName().trim().isEmpty()) {
            throw new ValidationException("File name is required");
        }
        if (processedFile.getStatus() == null || processedFile.getStatus().trim().isEmpty()) {
            throw new ValidationException("Status is required");
        }
        if (processedFile.getInterfaceEntity() == null || processedFile.getInterfaceEntity().getId() == null) {
            throw new ValidationException("Interface is required");
        }
        if (processedFile.getClient() == null || processedFile.getClient().getId() == null) {
            throw new ValidationException("Client is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFiles(int page, int size, String sortBy, String sortDirection, 
                                               String searchTerm, String status, LocalDateTime startDate, LocalDateTime endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (status != null && startDate != null && endDate != null) {
            return processedFileRepository.findByStatusAndProcessedAtBetween(status, startDate, endDate, pageable);
        } else if (status != null) {
            return processedFileRepository.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            return processedFileRepository.findByProcessedAtBetween(startDate, endDate, pageable);
        } else {
            return processedFileRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return processedFileRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, 
                                                                int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return processedFileRepository.findByClient_IdAndStatus(clientId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate,
                                                                   int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return processedFileRepository.findByClient_IdAndProcessedAtBetween(clientId, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedFile> getProcessedFilesByStatus(String status) {
        return processedFileRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClient(Long clientId, PageRequest pageRequest) {
        return processedFileRepository.findByClient_Id(clientId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> searchProcessedFiles(String fileName, PageRequest pageRequest) {
        return processedFileRepository.findByFileNameContainingIgnoreCase(fileName, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByStatus(String status, PageRequest pageRequest) {
        return processedFileRepository.findByStatus(status, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate, PageRequest pageRequest) {
        return processedFileRepository.findByProcessedAtBetween(startDate, endDate, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClientAndStatus(Long clientId, String status, PageRequest pageRequest) {
        return processedFileRepository.findByClient_IdAndStatus(clientId, status, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFilesByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate, PageRequest pageRequest) {
        return processedFileRepository.findByClient_IdAndProcessedAtBetween(clientId, startDate, endDate, pageRequest);
    }
} 