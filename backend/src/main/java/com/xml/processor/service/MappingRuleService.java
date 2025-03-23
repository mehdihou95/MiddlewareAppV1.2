package com.xml.processor.service;

import com.xml.processor.model.MappingRule;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MappingRuleService {
    @Cacheable(value = "mappingRules", key = "#id")
    MappingRule getMappingRuleById(Long id);

    @Cacheable(value = "mappingRules", key = "'all'")
    Page<MappingRule> getAllMappingRules(Pageable pageable);

    @Cacheable(value = "mappingRules", key = "'client_' + #clientId")
    Page<MappingRule> getMappingRulesByClient(Long clientId, Pageable pageable);

    @Cacheable(value = "mappingRules", key = "'interface_' + #interfaceId")
    Page<MappingRule> getMappingRulesByInterface(Long interfaceId, Pageable pageable);

    @Cacheable(value = "mappingRules", key = "'client_interface_' + #clientId + '_' + #interfaceId")
    Page<MappingRule> getMappingRulesByClientAndInterface(Long clientId, Long interfaceId, Pageable pageable);

    @CacheEvict(value = "mappingRules", allEntries = true)
    MappingRule createMappingRule(MappingRule mappingRule);

    @CacheEvict(value = "mappingRules", key = "#id")
    MappingRule updateMappingRule(Long id, MappingRule mappingRule);

    @CacheEvict(value = "mappingRules", key = "#id")
    void deleteMappingRule(Long id);

    Page<MappingRule> getMappingRules(int page, int size, String sortBy, String sortDirection, String searchTerm, Boolean isActive);
    Page<MappingRule> getMappingRulesByInterface(Long interfaceId, int page, int size, String sortBy, String sortDirection);
    Page<MappingRule> searchMappingRules(String searchTerm, int page, int size, String sortBy, String sortDirection);
    Page<MappingRule> getMappingRulesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection);
    List<MappingRule> getActiveMappingRules(Long interfaceId);
} 