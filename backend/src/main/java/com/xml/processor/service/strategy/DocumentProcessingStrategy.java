package com.xml.processor.service.strategy;

import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import org.w3c.dom.Document;

import java.util.Map;

public interface DocumentProcessingStrategy {
    /**
     * Process the XML document according to the specific strategy implementation
     * @param document The XML document to process
     * @param interfaceEntity The interface configuration
     * @param clientId The client ID
     * @return A map of processed data
     */
    Map<String, Object> processDocument(Document document, Interface interfaceEntity, Long clientId);

    /**
     * Validate if this strategy can handle the given interface type
     * @param interfaceType The type of interface to check
     * @return true if this strategy can handle the interface type
     */
    boolean canHandle(String interfaceType);

    /**
     * Get the priority of this strategy (higher priority strategies are checked first)
     * @return The priority value
     */
    int getPriority();

    /**
     * Get the name of this strategy
     * @return The strategy name
     */
    String getName();
} 