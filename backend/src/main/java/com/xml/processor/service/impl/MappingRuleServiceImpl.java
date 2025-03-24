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
import org.springframework.data.domain.PageImpl;
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
    public Optional<MappingRule> getMappingRuleById(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return mappingRuleRepository.findByIdAndClient_Id(id, clientId);
        }
        return mappingRuleRepository.findById(id);
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
    
    @Override
    @Transactional
    public void saveMappingConfiguration(List<MappingRule> rules) {
        mappingRuleRepository.saveAll(rules);
    }
    
    @Override
    @Transactional
    public void deleteByClient_IdAndTableName(Long clientId, String tableName) {
        mappingRuleRepository.deleteByClient_IdAndTableName(clientId, tableName);
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
    public Page<MappingRule> searchMappingRules(String name, Pageable pageable) {
        return mappingRuleRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByStatus(boolean isActive, Pageable pageable) {
        return mappingRuleRepository.findByIsActive(isActive, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRules(Pageable pageable, String nameFilter, Boolean isActiveFilter) {
        if (nameFilter != null && isActiveFilter != null) {
            // TODO: Add repository method for combined filter
            return mappingRuleRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else if (nameFilter != null) {
            return mappingRuleRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else if (isActiveFilter != null) {
            return mappingRuleRepository.findByIsActive(isActiveFilter, pageable);
        }
        return mappingRuleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByClientId(Long clientId, Pageable pageable) {
        return mappingRuleRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> findByTableNameAndClient_Id(String tableName, Long clientId, Pageable pageable) {
        List<MappingRule> rules = mappingRuleRepository.findByTableNameAndClient_Id(tableName, clientId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rules.size());
        List<MappingRule> pageContent = rules.subList(start, end);
        return new PageImpl<>(pageContent, pageable, rules.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getActiveMappingRules(Long interfaceId, Pageable pageable) {
        Interface interfaceEntity = interfaceRepository.findById(interfaceId)
            .orElseThrow(() -> new RuntimeException("Interface not found with id: " + interfaceId));
        
        Long clientId = interfaceEntity.getClient().getId();
        List<MappingRule> rules = mappingRuleRepository.findByClient_IdAndIsActive(clientId, true);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rules.size());
        List<MappingRule> pageContent = rules.subList(start, end);
        return new PageImpl<>(pageContent, pageable, rules.size());
    }
} 