package com.xml.processor.service.impl;

import com.xml.processor.model.Client;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.ClientOnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class ClientOnboardingServiceImpl implements ClientOnboardingService {

    private final ClientRepository clientRepository;
    private final MappingRuleRepository mappingRuleRepository;
    private final ObjectMapper objectMapper;
    private final YAMLFactory yamlFactory;

    @Autowired
    public ClientOnboardingServiceImpl(ClientRepository clientRepository, MappingRuleRepository mappingRuleRepository) {
        this.clientRepository = clientRepository;
        this.mappingRuleRepository = mappingRuleRepository;
        this.objectMapper = new ObjectMapper();
        this.yamlFactory = new YAMLFactory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> getDefaultMappingRules() {
        return mappingRuleRepository.findByIsDefaultTrue();
    }

    @Override
    @Transactional
    public Client onboardNewClient(Client client, List<MappingRule> defaultRules) {
        Client savedClient = clientRepository.save(client);
        defaultRules.forEach(rule -> {
            rule.setClient(savedClient);
            mappingRuleRepository.save(rule);
        });
        return savedClient;
    }

    @Override
    @Transactional
    public Client cloneClientConfiguration(Long sourceClientId, Client newClient) {
        Client sourceClient = clientRepository.findById(sourceClientId)
                .orElseThrow(() -> new RuntimeException("Source client not found"));
        
        Client savedClient = clientRepository.save(newClient);
        
        List<MappingRule> sourceRules = mappingRuleRepository.findByClient(sourceClient);
        sourceRules.forEach(rule -> {
            MappingRule newRule = new MappingRule(rule);
            newRule.setClient(savedClient);
            mappingRuleRepository.save(newRule);
        });
        
        return savedClient;
    }

    @Override
    @Transactional
    public Client onboardClient(Client client, MultipartFile[] configFiles) {
        if (configFiles != null && configFiles.length > 0) {
            processClientConfiguration(client.getId(), configFiles);
        }
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client updateClientConfiguration(Long clientId, MultipartFile[] configFiles) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        if (configFiles != null && configFiles.length > 0) {
            processClientConfiguration(clientId, configFiles);
        }
        
        return clientRepository.save(client);
    }

    @Override
    public boolean validateClientConfiguration(MultipartFile[] configFiles) {
        if (configFiles == null || configFiles.length == 0) {
            return true;
        }

        for (MultipartFile file : configFiles) {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            if (!filename.endsWith(".yaml") && !filename.endsWith(".yml")) {
                log.error("Invalid configuration file format: {}", filename);
                return false;
            }

            try {
                Map<String, Object> config = objectMapper.readValue(yamlFactory.createParser(file.getInputStream()), Map.class);
                if (!isValidConfiguration(config)) {
                    log.error("Invalid configuration structure in file: {}", filename);
                    return false;
                }
            } catch (IOException e) {
                log.error("Error reading configuration file: {}", filename, e);
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void processClientConfiguration(Long clientId, MultipartFile[] configFiles) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        for (MultipartFile file : configFiles) {
            try {
                Map<String, Object> config = objectMapper.readValue(yamlFactory.createParser(file.getInputStream()), Map.class);
                processConfiguration(client, config);
            } catch (IOException e) {
                log.error("Error processing configuration file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Error processing configuration file", e);
            }
        }
    }

    private boolean isValidConfiguration(Map<String, Object> config) {
        // Check for required configuration sections
        return config.containsKey("mapping_rules") && 
               config.containsKey("interfaces") && 
               config.containsKey("settings");
    }

    private void processConfiguration(Client client, Map<String, Object> config) {
        // Process mapping rules
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mappingRules = (List<Map<String, Object>>) config.get("mapping_rules");
        if (mappingRules != null) {
            mappingRules.forEach(ruleConfig -> {
                MappingRule rule = new MappingRule();
                rule.setClient(client);
                rule.setName((String) ruleConfig.get("name"));
                rule.setDescription((String) ruleConfig.get("description"));
                rule.setXmlPath((String) ruleConfig.get("xml_path"));
                rule.setDatabaseField((String) ruleConfig.get("database_field"));
                rule.setTransformation((String) ruleConfig.get("transformation"));
                rule.setRequired((Boolean) ruleConfig.get("required"));
                rule.setDefaultValue((String) ruleConfig.get("default_value"));
                rule.setPriority((Integer) ruleConfig.get("priority"));
                rule.setSourceField((String) ruleConfig.get("source_field"));
                rule.setTargetField((String) ruleConfig.get("target_field"));
                rule.setValidationRule((String) ruleConfig.get("validation_rule"));
                rule.setIsActive((Boolean) ruleConfig.get("is_active"));
                rule.setTableName((String) ruleConfig.get("table_name"));
                rule.setDataType((String) ruleConfig.get("data_type"));
                rule.setIsAttribute((Boolean) ruleConfig.get("is_attribute"));
                rule.setXsdElement((String) ruleConfig.get("xsd_element"));
                mappingRuleRepository.save(rule);
            });
        }

        // Process interfaces
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> interfaces = (List<Map<String, Object>>) config.get("interfaces");
        if (interfaces != null) {
            // TODO: Implement interface processing
            log.info("Interface processing not yet implemented");
        }

        // Process settings
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) config.get("settings");
        if (settings != null) {
            // TODO: Implement settings processing
            log.info("Settings processing not yet implemented");
        }
    }
} 