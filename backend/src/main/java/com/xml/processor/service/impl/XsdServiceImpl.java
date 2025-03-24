package com.xml.processor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.XsdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of XsdService.
 * Provides operations for validating and processing XSD files.
 */
@Service
public class XsdServiceImpl implements XsdService {

    @Autowired
    private MappingRuleRepository mappingRuleRepository;

    @Override
    public boolean validateXsdSchema(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(file.getInputStream());
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
    }

    @Override
    public Interface processXsdSchema(MultipartFile file, Interface interfaceEntity) {
        if (!validateXsdSchema(file)) {
            throw new ValidationException("Invalid XSD schema");
        }

        String rootElement = getRootElement(file);
        String namespace = getNamespace(file);

        interfaceEntity.setRootElement(rootElement);
        interfaceEntity.setNamespace(namespace);
        interfaceEntity.setSchemaPath(file.getOriginalFilename());

        return interfaceEntity;
    }

    @Override
    public String getRootElement(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            Element root = document.getDocumentElement();
            return root.getLocalName();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ValidationException("Failed to get root element from XSD schema", e);
        }
    }

    @Override
    public String getNamespace(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            Element root = document.getDocumentElement();
            return root.getNamespaceURI();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ValidationException("Failed to get namespace from XSD schema", e);
        }
    }

    @Override
    public List<Map<String, Object>> getXsdStructure(String xsdPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xsdPath);
            Element root = document.getDocumentElement();
            
            List<Map<String, Object>> structure = new ArrayList<>();
            Map<String, Object> rootInfo = Map.of(
                "name", root.getLocalName(),
                "namespace", root.getNamespaceURI(),
                "type", "root"
            );
            structure.add(rootInfo);
            
            // Analyze child elements
            NodeList elements = root.getElementsByTagName("xsd:element");
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                Map<String, Object> elementInfo = Map.of(
                    "name", element.getAttribute("name"),
                    "type", element.getAttribute("type"),
                    "minOccurs", element.getAttribute("minOccurs"),
                    "maxOccurs", element.getAttribute("maxOccurs")
                );
                structure.add(elementInfo);
            }
            
            return structure;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ValidationException("Failed to analyze XSD structure", e);
        }
    }

    @Override
    public List<Map<String, Object>> getXsdStructure(String xsdPath, Long clientId) {
        List<Map<String, Object>> structure = getXsdStructure(xsdPath);
        
        // Add client-specific customizations
        List<MappingRule> clientRules = mappingRuleRepository.findByClient_Id(clientId);
        for (MappingRule rule : clientRules) {
            Map<String, Object> ruleInfo = Map.of(
                "name", rule.getName(),
                "xmlPath", rule.getXmlPath(),
                "databaseField", rule.getDatabaseField(),
                "type", "mapping_rule"
            );
            structure.add(ruleInfo);
        }
        
        return structure;
    }

    @Override
    public Page<MappingRule> getAllMappingRules(Pageable pageable) {
        return mappingRuleRepository.findAll(pageable);
    }

    @Override
    public Page<MappingRule> getMappingRulesByClient(Long clientId, Pageable pageable) {
        return mappingRuleRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    public Page<MappingRule> getMappingRulesByInterface(Long interfaceId, Pageable pageable) {
        return mappingRuleRepository.findByInterfaceId(interfaceId, pageable);
    }

    @Override
    public Page<MappingRule> getMappingRulesByClientAndInterface(Long clientId, Long interfaceId, Pageable pageable) {
        return mappingRuleRepository.findByClient_IdAndInterfaceEntity_Id(clientId, interfaceId, pageable);
    }

    @Override
    public MappingRule saveMappingRule(MappingRule rule) {
        return mappingRuleRepository.save(rule);
    }

    @Override
    public void deleteMappingRule(Long id) {
        mappingRuleRepository.deleteById(id);
    }

    @Override
    public void saveMappingConfiguration(List<MappingRule> rules) {
        mappingRuleRepository.saveAll(rules);
    }

    @Override
    public List<MappingRule> getActiveMappingRules(Long interfaceId) {
        return mappingRuleRepository.findByInterfaceIdAndIsActiveTrue(interfaceId);
    }

    @Override
    public List<MappingRule> findByTableNameAndClient_Id(String tableName, Long clientId) {
        return mappingRuleRepository.findByTableNameAndClient_Id(tableName, clientId);
    }

    @Override
    public void deleteByClient_IdAndTableName(Long clientId, String tableName) {
        mappingRuleRepository.deleteByClient_IdAndTableName(clientId, tableName);
    }

    @Override
    public String analyzeXsdStructure(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.getInputStream());
            Element root = document.getDocumentElement();
            
            StringBuilder analysis = new StringBuilder();
            analysis.append("XSD Schema Analysis:\n");
            analysis.append("Root Element: ").append(root.getLocalName()).append("\n");
            analysis.append("Namespace: ").append(root.getNamespaceURI()).append("\n");
            
            // Analyze child elements
            NodeList elements = root.getElementsByTagName("xsd:element");
            analysis.append("\nElements found: ").append(elements.getLength()).append("\n");
            
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                String name = element.getAttribute("name");
                String minOccurs = element.getAttribute("minOccurs");
                String maxOccurs = element.getAttribute("maxOccurs");
                
                analysis.append("\nElement: ").append(name);
                if (!minOccurs.isEmpty()) {
                    analysis.append(", minOccurs: ").append(minOccurs);
                }
                if (!maxOccurs.isEmpty()) {
                    analysis.append(", maxOccurs: ").append(maxOccurs);
                }
                analysis.append("\n");
            }
            
            return analysis.toString();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ValidationException("Failed to analyze XSD structure", e);
        }
    }

    @Override
    public String analyzeXsdStructureWithClient(MultipartFile file, Long clientId) {
        // First get the basic structure analysis
        String basicAnalysis = analyzeXsdStructure(file);
        
        // Add client-specific analysis
        StringBuilder clientAnalysis = new StringBuilder(basicAnalysis);
        clientAnalysis.append("\nClient-specific Analysis (Client ID: ").append(clientId).append("):\n");
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(file.getInputStream());
            
            // Add client-specific validation rules or customizations
            List<MappingRule> clientRules = mappingRuleRepository.findByClient_Id(clientId);
            clientAnalysis.append("\nClient-specific Mapping Rules:\n");
            for (MappingRule rule : clientRules) {
                clientAnalysis.append("- ").append(rule.getName())
                    .append(" (").append(rule.getXmlPath())
                    .append(" -> ").append(rule.getDatabaseField())
                    .append(")\n");
            }
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ValidationException("Failed to analyze XSD structure with client context", e);
        }
        
        return clientAnalysis.toString();
    }

    @Override
    public List<MappingRule> getMappingRules(Long clientId) {
        return mappingRuleRepository.findByClient_Id(clientId);
    }

    @Override
    public List<MappingRule> getMappingRulesByTableName(String tableName) {
        return mappingRuleRepository.findByTableNameAndClient_Id(tableName, null);
    }

    @Override
    public List<MappingRule> getActiveMappingRules(Long clientId, Pageable pageable) {
        List<MappingRule> rules = mappingRuleRepository.findByClient_IdAndIsActive(clientId, true);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rules.size());
        return rules.subList(start, end);
    }

    @Override
    public void saveMappingConfiguration(Long clientId, String configuration) {
        try {
            // Parse the configuration string (assuming it's JSON)
            ObjectMapper mapper = new ObjectMapper();
            List<MappingRule> rules = mapper.readValue(configuration, 
                mapper.getTypeFactory().constructCollectionType(List.class, MappingRule.class));
            
            // Set client for all rules
            Client client = new Client();
            client.setId(clientId);
            rules.forEach(rule -> rule.setClient(client));
            
            // Save all rules
            mappingRuleRepository.saveAll(rules);
        } catch (IOException e) {
            throw new ValidationException("Failed to parse mapping configuration", e);
        }
    }

    @Override
    public void deleteMappingRulesByClientAndTable(Long clientId, String tableName) {
        mappingRuleRepository.deleteByClient_IdAndTableName(clientId, tableName);
    }
} 