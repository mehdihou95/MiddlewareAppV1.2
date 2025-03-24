package com.xml.processor.service.strategy;

import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AsnDocumentProcessingStrategy extends AbstractDocumentProcessingStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(AsnDocumentProcessingStrategy.class);
    private static final String ASN_TYPE = "ASN";
    
    // ASN-specific date formats
    private static final SimpleDateFormat ASN_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat ASN_TIME_FORMAT = new SimpleDateFormat("HHmmss");
    private static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat OUTPUT_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    @Override
    public String getDocumentType() {
        return ASN_TYPE;
    }
    
    @Override
    public boolean canHandle(String interfaceType) {
        return ASN_TYPE.equalsIgnoreCase(interfaceType);
    }
    
    @Override
    public String getName() {
        return "ASN Document Processor";
    }

    @Override
    public ProcessedFile processDocument(MultipartFile file, Interface interfaceEntity) {
        try {
            ProcessedFile processedFile = new ProcessedFile();
            processedFile.setFileName(file.getOriginalFilename());
            processedFile.setInterfaceEntity(interfaceEntity);
            processedFile.setClient(interfaceEntity.getClient());
            processedFile.setProcessedAt(LocalDateTime.now());
            processedFile.setStatus("SUCCESS");
            return processedFile;
        } catch (Exception e) {
            logger.error("Error processing ASN document: {}", e.getMessage(), e);
            ProcessedFile errorFile = new ProcessedFile();
            errorFile.setFileName(file.getOriginalFilename());
            errorFile.setInterfaceEntity(interfaceEntity);
            errorFile.setClient(interfaceEntity.getClient());
            errorFile.setProcessedAt(LocalDateTime.now());
            errorFile.setStatus("ERROR");
            errorFile.setErrorMessage(e.getMessage());
            return errorFile;
        }
    }
    
    @Override
    protected String applyTransformation(String value, String transformation) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        try {
            switch (transformation.toLowerCase()) {
                case "asn_date":
                    // Convert ASN date format (YYYYMMDD) to standard format (YYYY-MM-DD)
                    return OUTPUT_DATE_FORMAT.format(ASN_DATE_FORMAT.parse(value));
                    
                case "asn_time":
                    // Convert ASN time format (HHMMSS) to standard format (HH:MM:SS)
                    return OUTPUT_TIME_FORMAT.format(ASN_TIME_FORMAT.parse(value));
                    
                case "asn_number":
                    // Handle ASN-specific number format (remove leading zeros)
                    return String.valueOf(Long.parseLong(value));
                    
                case "asn_quantity":
                    // Handle ASN quantity format (decimal with 3 decimal places)
                    double quantity = Double.parseDouble(value);
                    return String.format("%.3f", quantity);
                    
                case "asn_status":
                    // Map ASN status codes to readable values
                    switch (value.trim()) {
                        case "01": return "NEW";
                        case "02": return "PROCESSING";
                        case "03": return "COMPLETED";
                        case "04": return "ERROR";
                        default: return value;
                    }
                    
                default:
                    // Fall back to parent class transformation
                    return super.applyTransformation(value, transformation);
            }
        } catch (ParseException e) {
            logger.error("Error parsing ASN date/time value: {}", value, e);
            return value;
        } catch (NumberFormatException e) {
            logger.error("Error parsing ASN number value: {}", value, e);
            return value;
        } catch (Exception e) {
            logger.error("Error applying ASN transformation {} to value {}: {}", transformation, value, e.getMessage());
            return value;
        }
    }
    
    @Override
    public int getPriority() {
        return 100; // Higher priority for ASN documents
    }
} 