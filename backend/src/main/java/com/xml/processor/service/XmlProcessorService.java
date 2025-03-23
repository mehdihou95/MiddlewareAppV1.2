package com.xml.processor.service;

import com.xml.processor.model.ProcessedFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface XmlProcessorService {
    ProcessedFile processXmlFile(MultipartFile file, Long interfaceId);
    ProcessedFile processXmlFile(MultipartFile file);
    CompletableFuture<ProcessedFile> processXmlFileAsync(MultipartFile file, Long interfaceId);
    void reprocessFile(Long fileId);
    List<ProcessedFile> getProcessedFiles();
    List<ProcessedFile> getErrorFiles();
} 