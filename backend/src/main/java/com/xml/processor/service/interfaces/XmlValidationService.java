package com.xml.processor.service.interfaces;

import org.w3c.dom.Document;

/**
 * Service interface for XML validation operations.
 */
public interface XmlValidationService {
    /**
     * Validates an XML document against an XSD schema.
     */
    boolean validateXmlAgainstXsd(Document document, String xsdContent);

    /**
     * Validates the structure of an XML document.
     */
    boolean validateXmlStructure(Document document);

    /**
     * Validates the content of an XML document against business rules.
     */
    boolean validateXmlContent(Document document, String interfaceType);

    /**
     * Gets the validation error message from the last validation operation.
     */
    String getValidationErrorMessage();
} 