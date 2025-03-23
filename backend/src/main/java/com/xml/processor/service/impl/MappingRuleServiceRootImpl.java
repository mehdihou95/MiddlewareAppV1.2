package com.xml.processor.service.impl;

import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.MappingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("mappingRuleServiceRoot")
public class MappingRuleServiceRootImpl implements MappingRuleService {

    @Autowired
    private MappingRuleRepository mappingRuleRepository;

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
    public List<MappingRule> getActiveMappingRules(Long interfaceId) {
        return mappingRuleRepository.findByInterfaceIdAndIsActive(interfaceId, true);
    }

    @Override
    @Transactional
    public MappingRule createMappingRule(MappingRule mappingRule) {
        return mappingRuleRepository.save(mappingRule);
    }

    @Override
    @Transactional
    public MappingRule updateMappingRule(Long id, MappingRule mappingRule) {
        MappingRule existingRule = mappingRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
        
        // Update fields
        existingRule.setName(mappingRule.getName());
        existingRule.setDescription(mappingRule.getDescription());
        existingRule.setXmlPath(mappingRule.getXmlPath());
        existingRule.setDatabaseField(mappingRule.getDatabaseField());
        existingRule.setTransformation(mappingRule.getTransformation());
        existingRule.setRequired(mappingRule.getRequired());
        existingRule.setDefaultValue(mappingRule.getDefaultValue());
        existingRule.setPriority(mappingRule.getPriority());
        existingRule.setSourceField(mappingRule.getSourceField());
        existingRule.setTargetField(mappingRule.getTargetField());
        existingRule.setValidationRule(mappingRule.getValidationRule());
        existingRule.setIsActive(mappingRule.getIsActive());
        existingRule.setTableName(mappingRule.getTableName());
        existingRule.setDataType(mappingRule.getDataType());
        existingRule.setIsAttribute(mappingRule.getIsAttribute());
        existingRule.setXsdElement(mappingRule.getXsdElement());
        
        if (mappingRule.getInterfaceEntity() != null) {
            existingRule.setInterfaceEntity(mappingRule.getInterfaceEntity());
        }
        
        return mappingRuleRepository.save(existingRule);
    }

    @Override
    @Transactional
    public void deleteMappingRule(Long id) {
        mappingRuleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRules(int page, int size, String sortBy, String direction, 
            String nameFilter, Boolean isActiveFilter) {
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        // Apply filters if provided
        if (nameFilter != null && !nameFilter.isEmpty()) {
            return mappingRuleRepository.findByNameContainingIgnoreCase(nameFilter, pageRequest);
        } else if (isActiveFilter != null) {
            return mappingRuleRepository.findByIsActive(isActiveFilter, pageRequest);
        }
        
        // No filters, return all with pagination
        return mappingRuleRepository.findAll(pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public MappingRule getMappingRuleById(Long id) {
        return mappingRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mapping rule not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByInterface(Long interfaceId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return mappingRuleRepository.findByInterfaceId(interfaceId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> searchMappingRules(String name, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return mappingRuleRepository.findByNameContainingIgnoreCase(name, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MappingRule> getMappingRulesByStatus(boolean isActive, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return mappingRuleRepository.findByIsActive(isActive, pageRequest);
    }
} 