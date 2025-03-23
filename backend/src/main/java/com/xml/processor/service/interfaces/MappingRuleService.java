package com.xml.processor.service.interfaces;

import com.xml.processor.model.MappingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MappingRuleService {
    Page<MappingRule> getAllMappingRules(Pageable pageable);
    Page<MappingRule> getMappingRulesByClient(Long clientId, Pageable pageable);
    Page<MappingRule> getMappingRulesByInterface(Long interfaceId, Pageable pageable);
    Page<MappingRule> getMappingRulesByClientAndInterface(Long clientId, Long interfaceId, Pageable pageable);
    List<MappingRule> getActiveMappingRules(Long interfaceId);
    MappingRule createMappingRule(MappingRule mappingRule);
    MappingRule updateMappingRule(Long id, MappingRule mappingRule);
    void deleteMappingRule(Long id);
    Page<MappingRule> getMappingRules(int page, int size, String sortBy, String direction, String nameFilter, Boolean isActiveFilter);
    MappingRule getMappingRuleById(Long id);
    Page<MappingRule> searchMappingRules(String name, int page, int size, String sortBy, String direction);
    Page<MappingRule> getMappingRulesByStatus(boolean isActive, int page, int size, String sortBy, String direction);
    List<MappingRule> getMappingRulesByClientId(Long clientId);
    List<MappingRule> findByTableNameAndClient_Id(String tableName, Long clientId);
    void deleteByClient_IdAndTableName(Long clientId, String tableName);
    void saveMappingConfiguration(List<MappingRule> rules);
} 