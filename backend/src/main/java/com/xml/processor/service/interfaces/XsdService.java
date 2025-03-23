package com.xml.processor.service.interfaces;

import com.xml.processor.model.MappingRule;
import java.util.List;
import java.util.Map;

public interface XsdService {
    List<Map<String, Object>> getXsdStructure(String xsdPath);
    List<Map<String, Object>> getXsdStructure(String xsdPath, Long clientId);
    List<MappingRule> getAllMappingRules();
    MappingRule saveMappingRule(MappingRule rule);
    void deleteMappingRule(Long id);
    void saveMappingConfiguration(List<MappingRule> rules);
} 