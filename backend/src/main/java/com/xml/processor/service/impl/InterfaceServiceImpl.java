package com.xml.processor.service.impl;

import com.xml.processor.exception.ResourceNotFoundException;
import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Interface;
import com.xml.processor.model.Client;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.InterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.io.StringReader;

/**
 * Primary implementation of the InterfaceService interface.
 * This service handles all interface-related operations including CRUD operations,
 * validation, and business logic for interface management.
 */
@Service
public class InterfaceServiceImpl implements InterfaceService {
    
    private final InterfaceRepository interfaceRepository;
    private final MappingRuleRepository mappingRuleRepository;
    
    @Autowired
    public InterfaceServiceImpl(InterfaceRepository interfaceRepository, MappingRuleRepository mappingRuleRepository) {
        this.interfaceRepository = interfaceRepository;
        this.mappingRuleRepository = mappingRuleRepository;
    }
    
    @Override
    public List<Interface> getAllInterfaces() {
        return interfaceRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Interface> getInterfaceById(Long id) {
        return interfaceRepository.findById(id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", allEntries = true)
    public Interface createInterface(Interface interfaceEntity) {
        validateInterface(interfaceEntity);
        return interfaceRepository.save(interfaceEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public Interface updateInterface(Long id, Interface interfaceEntity) {
        Interface existingInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + id));
        
        validateInterface(interfaceEntity);
        interfaceEntity.setId(id);
        return interfaceRepository.save(interfaceEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public void deleteInterface(Long id) {
        if (!interfaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interface not found with id: " + id);
        }
        interfaceRepository.deleteById(id);
    }
    
    @Override
    public List<Interface> getInterfacesByClient(Client client) {
        return interfaceRepository.findByClient(client);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Interface> getClientInterfaces(Long clientId) {
        return interfaceRepository.findByClient_Id(clientId);
    }
    
    @Override
    public Optional<Interface> getInterfaceByName(String name, Long clientId) {
        return interfaceRepository.findByNameAndClient_Id(name, clientId);
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "'all'")
    public Page<Interface> getAllInterfaces(Pageable pageable) {
        return interfaceRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Interface> getInterfacesByClient(Long clientId, Pageable pageable) {
        return interfaceRepository.findByClient_Id(clientId, pageable);
    }
    
    @Override
    public Page<Interface> getInterfacesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByClient_Id(clientId, pageRequest);
    }
    
    @Override
    public Page<Interface> getInterfaces(int page, int size, String sortBy, String sortDirection, 
                                       String searchTerm, Boolean isActive) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return interfaceRepository.findByNameContaining(searchTerm, pageRequest);
        } else if (isActive != null) {
            return interfaceRepository.findByIsActive(isActive, pageRequest);
        }
        
        return interfaceRepository.findAll(pageRequest);
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "'search_' + #name")
    public Page<Interface> searchInterfaces(String name, Pageable pageable) {
        return interfaceRepository.findByNameContaining(name, pageable);
    }
    
    @Override
    public Page<Interface> searchInterfaces(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByNameContaining(searchTerm, pageRequest);
    }
    
    @Override
    public Page<Interface> getInterfacesByType(String type, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByType(type, pageRequest);
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "'active_' + #isActive")
    public Page<Interface> getInterfacesByStatus(boolean isActive, Pageable pageable) {
        return interfaceRepository.findByIsActive(isActive, pageable);
    }
    
    @Override
    public Page<Interface> getInterfacesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByIsActive(isActive, pageRequest);
    }
    
    @Override
    public Interface detectInterface(String xmlContent, Long clientId) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlContent)));
            Element root = document.getDocumentElement();
            
            // Check root element name
            String rootName = root.getNodeName();
            
            // Check for specific elements that indicate interface type
            if (root.getElementsByTagName("InvoiceNumber").getLength() > 0) {
                return createInterface("INVOICE", rootName, clientId);
            } else if (root.getElementsByTagName("OrderNumber").getLength() > 0) {
                return createInterface("ORDER", rootName, clientId);
            } else if (root.getElementsByTagName("ShipmentNumber").getLength() > 0) {
                return createInterface("SHIPMENT", rootName, clientId);
            }
            
            // Default fallback based on root element
            if (rootName.contains("Invoice")) {
                return createInterface("INVOICE", rootName, clientId);
            } else if (rootName.contains("Order")) {
                return createInterface("ORDER", rootName, clientId);
            } else if (rootName.contains("Shipment")) {
                return createInterface("SHIPMENT", rootName, clientId);
            }
            
            throw new ValidationException("Could not detect interface type from XML content");
        } catch (Exception e) {
            throw new ValidationException("Failed to detect interface type: " + e.getMessage());
        }
    }
    
    private Interface createInterface(String type, String rootElement, Long clientId) {
        Interface interfaceEntity = new Interface();
        interfaceEntity.setType(type);
        interfaceEntity.setRootElement(rootElement);
        interfaceEntity.setNamespace("http://xml.processor.com/" + type.toLowerCase());
        interfaceEntity.setActive(true);
        interfaceEntity.setName(type + "_" + rootElement);
        
        Client client = new Client();
        client.setId(clientId);
        interfaceEntity.setClient(client);
        
        return interfaceEntity;
    }
    
    /**
     * Validates an interface entity before saving.
     *
     * @param interfaceEntity The interface to validate
     * @throws ValidationException if the interface is invalid
     */
    private void validateInterface(Interface interfaceEntity) {
        if (interfaceEntity.getName() == null || interfaceEntity.getName().trim().isEmpty()) {
            throw new ValidationException("Interface name is required");
        }

        if (interfaceEntity.getClient() == null || interfaceEntity.getClient().getId() == null) {
            throw new ValidationException("Client is required");
        }

        if (interfaceRepository.existsByNameAndClient_Id(interfaceEntity.getName(), interfaceEntity.getClient().getId())) {
            throw new ValidationException("Interface with name " + interfaceEntity.getName() + 
                " already exists for this client");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndClientId(String name, Long clientId) {
        return interfaceRepository.existsByNameAndClient_Id(name, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> getInterfaceMappings(Long interfaceId) {
        Interface interfaceEntity = interfaceRepository.findById(interfaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + interfaceId));
        return mappingRuleRepository.findByInterfaceId(interfaceId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#interfaceId")
    public List<MappingRule> updateInterfaceMappings(Long interfaceId, List<MappingRule> mappings) {
        Interface interfaceEntity = interfaceRepository.findById(interfaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + interfaceId));
        
        // Delete existing mappings
        mappingRuleRepository.deleteByInterfaceId(interfaceId);
        
        // Set interface reference and save new mappings
        List<MappingRule> savedMappings = new ArrayList<>();
        for (MappingRule mapping : mappings) {
            mapping.setInterfaceEntity(interfaceEntity);
            savedMappings.add(mappingRuleRepository.save(mapping));
        }
        
        return savedMappings;
    }
} 