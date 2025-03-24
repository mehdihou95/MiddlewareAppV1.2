package com.xml.processor.service;

import org.w3c.dom.Document;

public interface XmlValidationService {
    /**
     * Validates an XML document against an XSD schema
     * @param document The XML document to validate
     * @param xsdPath Path to the XSD schema file
     * @return true if validation succeeds, false otherwise
     */
    boolean validateXmlAgainstXsd(Document document, String xsdPath);

    /**
     * Validates the basic structure of an XML document
     * @param document The XML document to validate
     * @return true if structure is valid, false otherwise
     */
    boolean validateXmlStructure(Document document);

    /**
     * Validates the content of an XML document based on interface type
     * @param document The XML document to validate
     * @param interfaceType The type of interface (INVOICE, ORDER, SHIPMENT)
     * @return true if content is valid, false otherwise
     */
    boolean validateXmlContent(Document document, String interfaceType);

    /**
     * Gets the validation error message from the last validation operation
     * @return The error message, or null if there were no errors
     */
    String getValidationErrorMessage();
} 