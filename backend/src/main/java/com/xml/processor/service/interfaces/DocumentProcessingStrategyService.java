package com.xml.processor.service.interfaces;

import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.service.strategy.DocumentProcessingStrategy;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for document processing strategies.
 * Provides methods for processing documents using different strategies based on the interface type.
 */
public interface DocumentProcessingStrategyService {
    
    /**
     * Process a document using the appropriate strategy.
     *
     * @param file The document file to process
     * @param interfaceEntity The interface to process the file for
     * @return The processed file record
     */
    ProcessedFile processDocument(MultipartFile file, Interface interfaceEntity);

    /**
     * Get the appropriate processing strategy for the given interface type.
     *
     * @param interfaceType The type of interface
     * @return The processing strategy for the interface type
     */
    DocumentProcessingStrategy getStrategy(String interfaceType);
} 