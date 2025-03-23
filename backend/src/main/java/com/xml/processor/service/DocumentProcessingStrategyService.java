package com.xml.processor.service;

import com.xml.processor.model.Interface;
import com.xml.processor.service.strategy.DocumentProcessingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentProcessingStrategyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingStrategyService.class);
    
    @Autowired
    private List<DocumentProcessingStrategy> strategies;
    
    public DocumentProcessingStrategy getStrategy(Interface interfaceEntity) {
        if (interfaceEntity == null || interfaceEntity.getType() == null) {
            throw new IllegalArgumentException("Interface or interface type cannot be null");
        }
        
        // Sort strategies by priority (highest first)
        Optional<DocumentProcessingStrategy> strategy = strategies.stream()
            .filter(s -> s.canHandle(interfaceEntity.getType()))
            .max((s1, s2) -> Integer.compare(s1.getPriority(), s2.getPriority()));
        
        if (strategy.isEmpty()) {
            logger.error("No processing strategy found for interface type: {}", interfaceEntity.getType());
            throw new RuntimeException("No processing strategy found for interface type: " + interfaceEntity.getType());
        }
        
        return strategy.get();
    }
} 