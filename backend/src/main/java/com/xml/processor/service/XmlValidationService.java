package com.xml.processor.service;

import org.w3c.dom.Document;

public interface XmlValidationService {
    boolean validateXmlAgainstXsd(Document document, String xsdPath);
    boolean validateXmlStructure(Document document);
    boolean validateXmlContent(Document document, String interfaceType);
    String getValidationErrorMessage();
} 