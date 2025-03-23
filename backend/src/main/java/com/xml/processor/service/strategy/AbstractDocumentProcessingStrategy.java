package com.xml.processor.service.strategy;

import com.xml.processor.model.Interface;
import com.xml.processor.model.MappingRule;
import com.xml.processor.service.interfaces.MappingRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDocumentProcessingStrategy implements DocumentProcessingStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractDocumentProcessingStrategy.class);
    
    // Common date formats
    protected static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat ISO_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    protected static final SimpleDateFormat ISO_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    @Autowired
    protected MappingRuleService mappingRuleService;
    
    @Override
    public Map<String, Object> processDocument(Document document, Interface interfaceEntity, Long clientId) {
        Map<String, Object> result = new HashMap<>();
        List<MappingRule> rules = mappingRuleService.getActiveMappingRules(interfaceEntity.getId());
        
        XPath xPath = XPathFactory.newInstance().newXPath();
        
        for (MappingRule rule : rules) {
            try {
                String xmlPath = rule.getXmlPath();
                String databaseField = rule.getDatabaseField();
                String transformation = rule.getTransformation();
                String defaultValue = rule.getDefaultValue();
                
                // Evaluate XPath expression
                NodeList nodes = (NodeList) xPath.evaluate(xmlPath, document, XPathConstants.NODESET);
                String value = null;
                
                if (nodes != null && nodes.getLength() > 0) {
                    Node node = nodes.item(0);
                    value = node.getTextContent();
                    
                    // Apply transformation if specified
                    if (transformation != null && !transformation.isEmpty()) {
                        value = applyTransformation(value, transformation);
                    }
                } else if (defaultValue != null && !defaultValue.isEmpty()) {
                    value = defaultValue;
                }
                
                // Add to result map
                if (value != null) {
                    result.put(databaseField, value);
                } else if (rule.isRequired()) {
                    logger.warn("Required field {} not found in XML for rule {}", databaseField, rule.getName());
                }
                
            } catch (Exception e) {
                logger.error("Error processing mapping rule {}: {}", rule.getName(), e.getMessage(), e);
                if (rule.isRequired()) {
                    throw new RuntimeException("Failed to process required mapping rule: " + rule.getName(), e);
                }
            }
        }
        
        return result;
    }
    
    protected String applyTransformation(String value, String transformation) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        try {
            switch (transformation.toLowerCase()) {
                case "uppercase":
                    return value.toUpperCase();
                    
                case "lowercase":
                    return value.toLowerCase();
                    
                case "trim":
                    return value.trim();
                    
                case "date":
                    // Convert to ISO date format (YYYY-MM-DD)
                    return ISO_DATE_FORMAT.format(ISO_DATE_FORMAT.parse(value));
                    
                case "time":
                    // Convert to ISO time format (HH:MM:SS)
                    return ISO_TIME_FORMAT.format(ISO_TIME_FORMAT.parse(value));
                    
                case "datetime":
                    // Convert to ISO datetime format (YYYY-MM-DD'T'HH:MM:SS)
                    return ISO_DATETIME_FORMAT.format(ISO_DATETIME_FORMAT.parse(value));
                    
                case "number":
                    // Format number with 2 decimal places
                    double number = Double.parseDouble(value);
                    return String.format("%.2f", number);
                    
                case "integer":
                    // Convert to integer and remove decimal places
                    return String.valueOf((int) Double.parseDouble(value));
                    
                case "currency":
                    // Format as currency with 2 decimal places
                    double amount = Double.parseDouble(value);
                    return String.format("%.2f", amount);
                    
                default:
                    logger.warn("Unknown transformation type: {}", transformation);
                    return value;
            }
        } catch (ParseException e) {
            logger.error("Error parsing date/time value: {}", value, e);
            return value;
        } catch (NumberFormatException e) {
            logger.error("Error parsing number value: {}", value, e);
            return value;
        } catch (Exception e) {
            logger.error("Error applying transformation {} to value {}: {}", transformation, value, e.getMessage());
            return value;
        }
    }
    
    @Override
    public int getPriority() {
        return 0; // Default priority, can be overridden by implementations
    }
} 