package com.xml.processor.service.impl;

import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.interfaces.ProcessedFileService;
import com.xml.processor.service.interfaces.XmlProcessorService;
import com.xml.processor.service.interfaces.DocumentProcessingStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of XmlProcessorService.
 * Provides operations for validating, transforming, and processing XML files.
 */
@Service
public class XmlProcessorServiceImpl implements XmlProcessorService {

    @Autowired
    private ProcessedFileService processedFileService;

    @Autowired
    private InterfaceRepository interfaceRepository;

    @Autowired
    private DocumentProcessingStrategyService strategyService;

    @Override
    @Transactional
    public ProcessedFile processXmlFile(MultipartFile file, Interface interfaceEntity) {
        return strategyService.processDocument(file, interfaceEntity);
    }

    @Override
    public boolean validateXmlFile(MultipartFile file, Interface interfaceEntity) {
        try {
            ProcessedFile result = strategyService.processDocument(file, interfaceEntity);
            return "SUCCESS".equals(result.getStatus());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String transformXmlFile(MultipartFile file, Interface interfaceEntity) {
        ProcessedFile result = strategyService.processDocument(file, interfaceEntity);
        if (!"SUCCESS".equals(result.getStatus())) {
            throw new ValidationException("Failed to transform XML file: " + result.getErrorMessage());
        }
        return result.getContent();
    }

    @Override
    @Transactional
    public ProcessedFile processXmlFile(MultipartFile file) {
        // For now, throw UnsupportedOperationException as this method needs to be implemented
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    @Transactional
    public CompletableFuture<ProcessedFile> processXmlFileAsync(MultipartFile file, Long interfaceId) {
        return CompletableFuture.supplyAsync(() -> processXmlFile(file, interfaceRepository.findById(interfaceId).orElse(null)));
    }

    @Override
    @Transactional
    public void reprocessFile(Long fileId) {
        // For now, throw UnsupportedOperationException as this method needs to be implemented
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedFile> getProcessedFiles() {
        return processedFileService.getProcessedFilesByStatus("SUCCESS");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedFile> getErrorFiles() {
        return processedFileService.getProcessedFilesByStatus("ERROR");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getProcessedFiles(Pageable pageable) {
        return processedFileService.getProcessedFilesByStatus("SUCCESS", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcessedFile> getErrorFiles(Pageable pageable) {
        return processedFileService.getProcessedFilesByStatus("ERROR", pageable);
    }
} 