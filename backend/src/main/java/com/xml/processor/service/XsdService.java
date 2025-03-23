package com.xml.processor.service;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.Client;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.MappingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xml.processor.service.interfaces.ClientService;

@Service
public class XsdService {
    
    private static final Logger logger = LoggerFactory.getLogger(XsdService.class);
    
    @Autowired
    private MappingRuleRepository mappingRuleRepository;
    
    @Autowired
    private ClientService clientService;
    
    public List<Map<String, Object>> getXsdStructure(String xsdPath) {
        return getXsdStructure(xsdPath, ClientContextHolder.getClientId());
    }
    
    public List<Map<String, Object>> getXsdStructure(String xsdPath, Long clientId) {
        List<Map<String, Object>> elements = new ArrayList<>();
        try {
            logger.info("Loading XSD from path: {} for client: {}", xsdPath, clientId);
            
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");
            
            // Check for client-specific XSD first
            String clientSpecificPath = null;
            if (clientId != null) {
                clientSpecificPath = projectRoot + "/src/main/resources/clients/" + clientId + "/" + xsdPath;
                File clientSpecificFile = new File(clientSpecificPath);
                if (clientSpecificFile.exists()) {
                    logger.info("Using client-specific XSD at: {}", clientSpecificPath);
                    return parseXsdFile(clientSpecificFile, elements);
                }
            }
            
            // Fall back to default XSD
            String fullPath = projectRoot + "/src/main/resources/" + xsdPath;
            File xsdFile = new File(fullPath);
            
            if (!xsdFile.exists()) {
                logger.error("XSD file not found at: {}", fullPath);
                throw new RuntimeException("XSD file not found: " + fullPath);
            }
            
            return parseXsdFile(xsdFile, elements);
        } catch (Exception e) {
            logger.error("Error parsing XSD: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse XSD: " + e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> parseXsdFile(File xsdFile, List<Map<String, Object>> elements) throws Exception {
        logger.info("Reading XSD from file: {}", xsdFile.getAbsolutePath());
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xsdFile);
        
        Element root = document.getDocumentElement();
        logger.info("Root element: {}", root.getLocalName());
        
        parseElements(root, "", elements);
        logger.info("Found {} elements in XSD", elements.size());
        return elements;
    }
    
    private void parseElements(Element element, String path, List<Map<String, Object>> elements) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Element child = (Element) childNodes.item(i);
            if (child.getLocalName().equals("element")) {
                String name = child.getAttribute("name");
                String type = child.getAttribute("type");
                String fullPath = path.isEmpty() ? name : path + "." + name;
                
                Map<String, Object> elementInfo = Map.of(
                    "name", name,
                    "type", type,
                    "path", fullPath
                );
                elements.add(elementInfo);
                
                parseElements(child, fullPath, elements);
            }
        }
    }
    
    public List<MappingRule> getAllMappingRules() {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return mappingRuleRepository.findByClient_Id(clientId);
        }
        return mappingRuleRepository.findAll();
    }
    
    public MappingRule saveMappingRule(MappingRule rule) {
        // Set client from context if not explicitly set
        if (rule.getClient() == null && ClientContextHolder.getClientId() != null) {
            Client client = clientService.getClientById(ClientContextHolder.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
            rule.setClient(client);
        }
        return mappingRuleRepository.save(rule);
    }
    
    public void deleteMappingRule(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            // Only delete if rule belongs to current client
            mappingRuleRepository.findByIdAndClient_Id(id, clientId)
                .ifPresent(rule -> mappingRuleRepository.deleteById(id));
        } else {
            mappingRuleRepository.deleteById(id);
        }
    }

    public void saveMappingConfiguration(List<MappingRule> rules) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            // Delete only this client's rules
            mappingRuleRepository.deleteByClient_Id(clientId);
            
            // Set client for all rules
            Client client = clientService.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
            
            rules.forEach(rule -> rule.setClient(client));
        } else {
            // Delete all rules (admin operation)
            mappingRuleRepository.deleteAll();
        }
        
        // Save new rules
        mappingRuleRepository.saveAll(rules);
        logger.info("Saved {} mapping rules to configuration", rules.size());
    }
} 