package com.xml.processor.service.strategy;

import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

/**
 * Interface for document processing strategies.
 * Each strategy implements a specific way of processing documents based on their type.
 */
public interface DocumentProcessingStrategy {
    
    /**
     * Process a document using this strategy.
     *
     * @param file The document file to process
     * @param interfaceEntity The interface to process the file for
     * @return The processed file record
     */
    ProcessedFile processDocument(MultipartFile file, Interface interfaceEntity);

    /**
     * Process a document using this strategy.
     */
    ProcessedFile processDocument(Document document, Interface interfaceEntity, Long clientId);

    /**
     * Get the type of documents this strategy can process.
     *
     * @return The document type this strategy handles
     */
    String getDocumentType();

    /**
     * Check if this strategy can handle the given document type.
     */
    boolean canHandle(String documentType);

    /**
     * Get the name of this strategy.
     */
    String getName();

    /**
     * Get the priority of this strategy.
     */
    int getPriority();
} 