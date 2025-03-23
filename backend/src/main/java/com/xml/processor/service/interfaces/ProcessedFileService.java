package com.xml.processor.service.interfaces;
    
import com.xml.processor.model.ProcessedFile;
import java.util.List;
import java.util.Optional;
    
public interface ProcessedFileService {
    ProcessedFile createProcessedFile(ProcessedFile file);
    Optional<ProcessedFile> getProcessedFileByIdOptional(Long id);
    List<ProcessedFile> getAllProcessedFiles();
    List<ProcessedFile> getProcessedFilesByClient_Id(Long clientId);
    Optional<ProcessedFile> findByFileNameAndClient_Id(String fileName, Long clientId);
    List<ProcessedFile> findByStatus(String status);
    List<ProcessedFile> findByClient_IdAndStatus(Long clientId, String status);
    List<ProcessedFile> findByClient_IdAndProcessingStartTimeBetween(Long clientId, String startTime, String endTime);
    List<ProcessedFile> findByClient_IdAndHasErrors(Long clientId);
    List<ProcessedFile> findLatestProcessedFiles(Long clientId);
    ProcessedFile updateProcessedFile(Long id, ProcessedFile file);
    void deleteProcessedFile(Long id);
} 