package com.xml.processor.service.impl;

import com.xml.processor.model.ProcessedFile;
import com.xml.processor.repository.ProcessedFileRepository;
import com.xml.processor.service.interfaces.ProcessedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LegacyProcessedFileServiceImpl implements ProcessedFileService {

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Override
    public ProcessedFile createProcessedFile(ProcessedFile file) {
        return processedFileRepository.save(file);
    }

    @Override
    public Optional<ProcessedFile> getProcessedFileByIdOptional(Long id) {
        return processedFileRepository.findById(id);
    }

    @Override
    public List<ProcessedFile> getAllProcessedFiles() {
        return processedFileRepository.findAll();
    }

    @Override
    public List<ProcessedFile> getProcessedFilesByClient_Id(Long clientId) {
        return processedFileRepository.findByClient_Id(clientId);
    }

    @Override
    public Optional<ProcessedFile> findByFileNameAndClient_Id(String fileName, Long clientId) {
        return processedFileRepository.findByClient_IdAndFileName(clientId, fileName).stream().findFirst();
    }

    @Override
    public List<ProcessedFile> findByStatus(String status) {
        return processedFileRepository.findByStatus(status);
    }

    @Override
    public List<ProcessedFile> findByClient_IdAndStatus(Long clientId, String status) {
        return processedFileRepository.findByClient_IdAndStatus(clientId, status);
    }

    @Override
    public List<ProcessedFile> findByClient_IdAndProcessingStartTimeBetween(Long clientId, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return processedFileRepository.findByClient_IdAndProcessedAtBetween(clientId, start, end);
    }

    @Override
    public List<ProcessedFile> findByClient_IdAndHasErrors(Long clientId) {
        return processedFileRepository.findByClient_IdAndErrorMessageIsNotNull(clientId);
    }

    @Override
    public List<ProcessedFile> findLatestProcessedFiles(Long clientId) {
        return processedFileRepository.findTop10ByClient_IdOrderByProcessedAtDesc(clientId);
    }

    @Override
    public ProcessedFile updateProcessedFile(Long id, ProcessedFile file) {
        file.setId(id);
        return processedFileRepository.save(file);
    }

    @Override
    public void deleteProcessedFile(Long id) {
        processedFileRepository.deleteById(id);
    }
} 