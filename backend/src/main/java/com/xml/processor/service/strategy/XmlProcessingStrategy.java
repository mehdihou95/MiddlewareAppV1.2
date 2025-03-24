package com.xml.processor.service.strategy;

import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Interface;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.service.interfaces.ProcessedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * Strategy implementation for processing XML documents.
 */
@Component
public class XmlProcessingStrategy implements DocumentProcessingStrategy {

    @Autowired
    private ProcessedFileService processedFileService;

    @Override
    public ProcessedFile processDocument(MultipartFile file, Interface interfaceEntity) {
        if (!validateXmlFile(file, interfaceEntity)) {
            ProcessedFile errorFile = new ProcessedFile();
            errorFile.setFileName(file.getOriginalFilename());
            errorFile.setStatus("ERROR");
            errorFile.setErrorMessage("Invalid XML file");
            errorFile.setInterfaceEntity(interfaceEntity);
            errorFile.setClient(interfaceEntity.getClient());
            errorFile.setProcessedAt(LocalDateTime.now());
            return processedFileService.createProcessedFile(errorFile);
        }

        String transformedXml = transformXmlFile(file, interfaceEntity);

        ProcessedFile processedFile = new ProcessedFile();
        processedFile.setFileName(file.getOriginalFilename());
        processedFile.setStatus("SUCCESS");
        processedFile.setInterfaceEntity(interfaceEntity);
        processedFile.setClient(interfaceEntity.getClient());
        processedFile.setProcessedAt(LocalDateTime.now());
        processedFile.setContent(transformedXml);

        return processedFileService.createProcessedFile(processedFile);
    }

    @Override
    public ProcessedFile processDocument(Document document, Interface interfaceEntity, Long clientId) {
        try {
            ProcessedFile processedFile = new ProcessedFile();
            processedFile.setFileName("document.xml"); // Default name for Document objects
            processedFile.setInterfaceEntity(interfaceEntity);
            processedFile.setProcessedAt(LocalDateTime.now());
            processedFile.setStatus("SUCCESS");
            return processedFile;
        } catch (Exception e) {
            ProcessedFile errorFile = new ProcessedFile();
            errorFile.setFileName("document.xml");
            errorFile.setInterfaceEntity(interfaceEntity);
            errorFile.setProcessedAt(LocalDateTime.now());
            errorFile.setStatus("ERROR");
            errorFile.setErrorMessage(e.getMessage());
            return errorFile;
        }
    }

    private boolean validateXmlFile(MultipartFile file, Interface interfaceEntity) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            
            Element root = document.getDocumentElement();
            if (!root.getLocalName().equals(interfaceEntity.getRootElement())) {
                return false;
            }
            if (!root.getNamespaceURI().equals(interfaceEntity.getNamespace())) {
                return false;
            }
            
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
    }

    private String transformXmlFile(MultipartFile file, Interface interfaceEntity) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());

            // TODO: Implement XML transformation based on interface mapping rules

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            throw new ValidationException("Failed to transform XML file", e);
        }
    }

    @Override
    public String getDocumentType() {
        return "XML";
    }

    @Override
    public boolean canHandle(String documentType) {
        return "XML".equalsIgnoreCase(documentType);
    }

    @Override
    public String getName() {
        return "XML Document Processor";
    }

    @Override
    public int getPriority() {
        return 50; // Medium priority for XML documents
    }
} 