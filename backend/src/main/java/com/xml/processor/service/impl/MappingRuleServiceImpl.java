package com.xml.processor.service.impl;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.MappingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("mappingRuleService")
public class MappingRuleServiceImpl implements MappingRuleService {

    @Autowired
    private MappingRuleRepository mappingRuleRepository;
    
    @Autowired
    private InterfaceRepository interfaceRepository;

    @Override
    @Transactional
    public MappingRule createMappingRule(MappingRule mappingRule) {
        // Set client from context if not provided
        if (mappingRule.getClient() == null && ClientContextHolder.getClient() != null) {
            mappingRule.setClient(ClientContextHolder.getClient());
        }
        return mappingRuleRepository.save(mappingRule);
    }

    @Override
    @Transactional(readOnly = true)
    public MappingRule getMappingRuleById(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return mappingRuleRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
        }
        return mappingRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<MappingRule> getAllMappingRules() {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return mappingRuleRepository.findByClient_Id(clientId);
        }
        return mappingRuleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> getMappingRulesByClientId(Long clientId) {
        return mappingRuleRepository.findByClient_Id(clientId);
    }

    @Override
    @Transactional
    public MappingRule updateMappingRule(Long id, MappingRule mappingRuleDetails) {
        Long clientId = ClientContextHolder.getClientId();
        MappingRule mappingRule;
        
        if (clientId != null) {
            mappingRule = mappingRuleRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
        } else {
            mappingRule = mappingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
        }
        
        // Update mappingRule fields
        mappingRule.setName(mappingRuleDetails.getName());
        mappingRule.setDescription(mappingRuleDetails.getDescription());
        mappingRule.setXmlPath(mappingRuleDetails.getXmlPath());
        mappingRule.setDatabaseField(mappingRuleDetails.getDatabaseField());
        mappingRule.setTransformation(mappingRuleDetails.getTransformation());
        mappingRule.setRequired(mappingRuleDetails.getRequired());
        mappingRule.setDefaultValue(mappingRuleDetails.getDefaultValue());
        mappingRule.setPriority(mappingRuleDetails.getPriority());
        mappingRule.setSourceField(mappingRuleDetails.getSourceField());
        mappingRule.setTargetField(mappingRuleDetails.getTargetField());
        mappingRule.setValidationRule(mappingRuleDetails.getValidationRule());
        mappingRule.setIsActive(mappingRuleDetails.getIsActive());
        mappingRule.setTableName(mappingRuleDetails.getTableName());
        mappingRule.setDataType(mappingRuleDetails.getDataType());
        mappingRule.setIsAttribute(mappingRuleDetails.getIsAttribute());
        mappingRule.setXsdElement(mappingRuleDetails.getXsdElement());
        
        if (mappingRuleDetails.getInterfaceEntity() != null) {
            mappingRule.setInterfaceEntity(mappingRuleDetails.getInterfaceEntity());
        }
        
        return mappingRuleRepository.save(mappingRule);
    }

    @Override
    @Transactional
    public void deleteMappingRule(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            mappingRuleRepository.findByIdAndClient_Id(id, clientId)
                .ifPresent(rule -> mappingRuleRepository.deleteById(id));
        } else {
            mappingRuleRepository.deleteById(id);
        }
    }
    
    @Transactional
    public void deleteAllMappingRulesByClient(Long clientId) {
        mappingRuleRepository.deleteByClient_Id(clientId);
    }
    
    @Override
    @Transactional
    public void saveMappingConfiguration(List<MappingRule> rules) {
        mappingRuleRepository.saveAll(rules);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> findByTableNameAndClient_Id(String tableName, Long clientId) {
        return mappingRuleRepository.findByTableNameAndClient_Id(tableName, clientId);
    }
    
    @Override
    @Transactional
    public void deleteByClient_IdAndTableName(Long clientId, String tableName) {
        mappingRuleRepository.deleteByClient_IdAndTableName(clientId, tableName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> getActiveMappingRules(Long interfaceId) {
        Interface interfaceEntity = interfaceRepository.findById(interfaceId)
            .orElseThrow(() -> new RuntimeException("Interface not found with id: " + interfaceId));
        
        Long clientId = interfaceEntity.getClient().getId();
        return mappingRuleRepository.findByClient_IdAndIsActive(clientId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getAllMappingRules(Pageable pageable) {
        return mappingRuleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByClient(Long clientId, Pageable pageable) {
        return mappingRuleRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByInterface(Long interfaceId, Pageable pageable) {
        return mappingRuleRepository.findByInterfaceId(interfaceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByClientAndInterface(Long clientId, Long interfaceId, Pageable pageable) {
        return mappingRuleRepository.findByClient_IdAndInterfaceEntity_Id(clientId, interfaceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRules(int page, int size, String sortBy, String direction, String nameFilter, Boolean isActiveFilter) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        if (nameFilter != null && !nameFilter.isEmpty()) {
            return mappingRuleRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else if (isActiveFilter != null) {
            return mappingRuleRepository.findByIsActive(isActiveFilter, pageable);
        }
        
        return mappingRuleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> searchMappingRules(String name, int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return mappingRuleRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByStatus(boolean isActive, int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return mappingRuleRepository.findByIsActive(isActive, pageable);
    }
} 